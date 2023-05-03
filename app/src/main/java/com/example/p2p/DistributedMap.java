package com.example.p2p;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.jgroups.*;
import org.jgroups.blocks.cs.ReceiverAdapter;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;


public class DistributedMap implements SimpleStringMap{
    private Map<String, String> distrMap = new HashMap<>();
    private Map<String, ResourceMessage> resMap = new HashMap<>();
    private JChannel mapChannel;

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
                    default:
                        System.err.println("Received unknown command");
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

        resBroadcast(mapChannel, new ResourceMessage(ip, "0.6", "0.6", "0.6"));
    }

    public void resBroadcast(JChannel channel, ResourceMessage rm) throws Exception {
        channel.send(new ObjectMessage(null, "rb;;"+ rm.ip + ";;" + rm.cpu + ";;" + rm.gpu + ";;" + rm.disk));
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
//        mapChannel.send(new ObjectMessage(null, "p;;" + key + ";;" + value));
        mapChannel.send(new ObjectMessage(null, "p;;" + key + ";;" + value));
        return null;
    }

    @Override
    public String remove(String key) throws Exception {
//        mapChannel.send(new ObjectMessage(null, "r;;" + key));
        mapChannel.send(new ObjectMessage(null, "r;;" + key));
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
