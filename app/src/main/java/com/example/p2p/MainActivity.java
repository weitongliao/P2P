package com.example.p2p;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.protocols.pbcast.STATE;
import org.jgroups.stack.*;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button link = findViewById(R.id.link_button);
        Button aaa = findViewById(R.id.aaa);

        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LinkActivity.class);
                startActivity(intent);
            }
        });

        aaa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                NetworkTask nt = new NetworkTask();
                nt.execute();




//                 Wait for the message to be received
//                Thread.sleep(1000);
            }
        });

    }

    private class NetworkTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Protocol[] prot_stack= new Protocol[0];
            try {
                prot_stack = new Protocol[]{
                        //                bind local ip
                        //                new UDP().setValue("bind_addr", InetAddress.getByName(ip)).setValue("bind_port", 8000),
                        new UDP().setValue("bind_addr", InetAddress.getByName("172.20.10.2")).setValue("bind_port", 8000),
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
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            JChannel channel = null;
            try {
                channel = new JChannel(prot_stack);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                channel.connect("my-channel");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            channel.setReceiver(new Receiver() {
                @Override
                public void viewAccepted(org.jgroups.View view) {
                    System.out.println("Received view: " + view);
                }

                @Override
                public void receive(Message msg) {
                    System.out.println("Received message: " + msg.getObject());
                }
            });

            // Wait for the channel to finish connecting
//                Thread.sleep(1000);

            // Send a test message
            Message msg = new ObjectMessage(null, "Hello, worldbbb!");
            try {
                channel.send(msg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

//            try {
//                InetAddress serverAddress = InetAddress.getByName("192.168.137.1"); // 服务器地址
//                int serverPort = 8000; // 服务器端口
//                String message = "Hello, world!"; // 要发送的消息
//
//                DatagramSocket clientSocket = new DatagramSocket();
//                byte[] sendData = message.getBytes();
//                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
//                clientSocket.send(sendPacket);
//
//                System.out.println("Sent message: " + message);
//                clientSocket.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // 处理结果并更新UI
        }
    }

}

