package com.example.p2p;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jgroups.*;
import org.jgroups.blocks.cs.ReceiverAdapter;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;


public class DistributedMap implements SimpleStringMap{
    private Map<String, String> distrMap = new HashMap<>();
    private Map<String, ResourceMessage> resMap = new HashMap<>();
    private JChannel mapChannel;
    private static final short HEADER_ID = 50;
//    private static final short HEADER_ID = 1000;
//    static {
//        ClassConfigurator.addProtocol(HEADER_ID, FileHeader.class);
//    }

    public DistributedMap(String ip) throws Exception {
//        String ip = InetAddress.getLocalHost().getHostAddress();
        Protocol[] prot_stack={
//                bind local ip
                new UDP().setValue("bind_addr", InetAddress.getByName(ip)).setValue("bind_port", 8000),
                new PING(),
                new MERGE3(),
                new FD_SOCK(),
                new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000),
//                new VERIFY_SUSPECT(),
                new BARRIER(),
                new NAKACK2(),
                new UNICAST3(),
                new STABLE(),
                new GMS(),
                new UFC(),
                new MFC(),
                new FRAG2(),
                new STATE()};
        mapChannel = new JChannel(prot_stack);

        mapChannel.setReceiver(new Receiver(){
            @Override
            public void viewAccepted(View view) {
                System.out.println("view update" + view);
                if(view instanceof MergeView){
                    System.out.println("mergeview detected");
                    new MergeHandler(mapChannel, (MergeView)view).start();
                }
            }

            public void receive(Message msg) {
                FileHeader header = msg.getHeader(HEADER_ID);
                String fileName = header.getMessage();
                System.out.println(fileName);
                if(!Objects.equals(fileName, "")){
                    byte[] data = msg.getObject();
                    try {
                        // 将接收到的数据写入文件
                        FileOutputStream fos = new FileOutputStream(fileName);
                        fos.write(data);
                        fos.close();
                        System.out.println("Received file: " + fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    String txt = (String)msg.getObject();
                    String[] command = txt.split(";;");
                    switch(command[0]) {
                        case "p":
                            distrMap.put(command[1], command[2]);
                            break;
                        case "r":
                            distrMap.remove(command[1]);
//                        System.out.println(command[1] + " is removed");
                            break;
                        case "rb":
                            resMap.put(command[1], new ResourceMessage(command[1], command[2], command[3], command[4]));
                            System.out.println(command[1] +" CPU:"+ command[2]+ " GPU"+ command[3]+ " DISK:"+ command[4]);
                            break;
                        case "getfile":
                            try {
                                sendFile(command[1]);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        default:
                            System.err.println("Received unknown command");
                    }
                }
            }

            public void getState(OutputStream outputStream) throws Exception {
                synchronized (distrMap){
                    Util.objectToStream(distrMap, new DataOutputStream(outputStream));
                }
            }

            public void setState(InputStream inputStream) throws Exception {
                HashMap<String, String> hm = (HashMap<String, String>)Util.objectFromStream(new DataInputStream(inputStream));
                synchronized (distrMap){
                    distrMap.clear();
                    distrMap.putAll(hm);
                }
                System.out.println(distrMap.size() + " entries got from group:" + distrMap.toString());
            }
        });

        mapChannel.connect("DHTCluster");

        mapChannel.getState(null, 100000);

//        resBroadcast(mapChannel, new ResourceMessage(ip, "0.6", "0.6", "0.6"));
    }

    public void resBroadcast(JChannel channel, ResourceMessage rm) throws Exception {
        channel.send(new ObjectMessage(null, "rb;;"+ rm.ip + ";;" + rm.cpu + ";;" + rm.gpu + ";;" + rm.disk).putHeader(HEADER_ID, new FileHeader("")));
    }

    @Override
    public boolean containsKey(String key) {
        return distrMap.containsKey(key);
    }

    @Override
    public String get(String key) {
        return distrMap.get(key);
    }

    @Override
    public String put(String key, String value) throws Exception {
        mapChannel.send(new ObjectMessage(null, "p;;" + key + ";;" + value).putHeader(HEADER_ID, new FileHeader("")));
//        mapChannel.send(new Message(null, null, "p;;" + key + ";;" + value));
        return null;
    }

    public String getFile(String fileName) throws Exception {
        mapChannel.send(new ObjectMessage(null, "getfile;;" + fileName).putHeader(HEADER_ID, new FileHeader("")));
//        mapChannel.send(new Message(null, null, "p;;" + key + ";;" + value));
        return null;
    }

    public String sendFile(String file_name) throws Exception {
        try {
            // 读取文件内容
            FileInputStream fis = new FileInputStream(file_name);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int read;
            while ((read = fis.read(buf)) > 0) {
                bos.write(buf, 0, read);
            }
            fis.close();
            byte[] data = bos.toByteArray();

            // 创建消息，并设置文件名为消息头
            Message msg = new ObjectMessage(null, data);
            Header a = new FileHeader(file_name);
            msg.putHeader(HEADER_ID, a);

            // 发送消息
            mapChannel.send(msg);
            System.out.println("Sent file: " + file_name);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        mapChannel.send(new ObjectMessage(null, "p;;" + key + ";;" + value));
//        mapChannel.send(new Message(null, null, "p;;" + key + ";;" + value));
        return null;
    }

    @Override
    public String remove(String key) throws Exception {
        mapChannel.send(new ObjectMessage(null, "r;;" + key).putHeader(HEADER_ID, new FileHeader("")));
//        mapChannel.send(new Message(null, null, "r;;" + key));
        return null;
    }

    public void finish(){
        mapChannel.close();
    }

    private static class ResourceMessage {
        private final String ip;
        private final String cpu;
        private final String gpu;
        private final String disk;

        public ResourceMessage(String ip, String cpu, String gpu, String disk) {
            this.ip = ip;
            this.cpu = cpu;
            this.gpu = gpu;
            this.disk = disk;
        }
    }
}
