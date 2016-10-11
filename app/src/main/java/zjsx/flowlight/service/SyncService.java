package zjsx.flowlight.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;

import zjsx.flowlight.msg.MsgParser;
import zjsx.flowlight.netty.TcpClient;
import zjsx.flowlight.netty.TcpServer;

import static zjsx.flowlight.common.Utils.*;

/**
 * Created by Admin on 2016/10/10.
 */

public class SyncService extends Service {

    static String IP = "239.0.0.1";
    static int PORT = 54321;

    MulticastSocket multicastSocket;
    MulticastReceiveThread mMulticastReceiveThread;
    MulticastSendThread mMulticastSendThread;

    public class SyncBinder extends Binder{
        public SyncService getService(){
            SYNCSERVICE = SyncService.this;
            return SyncService.this;
        }
    }

    SyncBinder mBinder = new SyncBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        SYNCSERVICE = null;
        mMulticastReceiveThread.close();
        mMulticastSendThread.close();
        multicastSocket.close();
        if(mTcpClient != null && mTcpClient.isConnected()){
            mTcpClient.disconnect();
        }

        if(mTcpServer != null && mTcpServer.isLaunched()){
            mTcpServer.shutDown();
        }
        return super.onUnbind(intent);
    }

    //监听和发送多播信息
    public void startMulticastReceiver(){
        joinMulticatGroup();
    }


    TcpServer mTcpServer = new TcpServer();
    //启动TCP服务器
    public void startTCPServer(){
        if(!mTcpServer.isLaunched()) {
            mTcpServer.startService(new TcpServer.ServerListener() {
                @Override
                public void onLaunchFinished(boolean success) {
                    if(success) {
                        show("TCP服务启动成功");
                    }else{
                        show("TCP服务启动失败");
                    }
                }
            });
        }else{
            show("TCP服务已经启动");
        }
    }

    TcpClient mTcpClient;

    //启动TCP连接器
    public void connectTcpServer(final String host, final int port){
        if(mTcpClient != null && mTcpClient.isConnected()){
            show("TCP客户端已经连接到了服务器");
        }else {
            mTcpClient = new TcpClient(host, port);
            mTcpClient.connect(false, true, new TcpClient.ClientListener() {
                @Override
                public void onConnectFinished(boolean reconnect, boolean success) {
                    if(success){
                        show("TCP客户端成功连接服务器"+host+":"+port);
                    }else{
                        show("TCP客户端连接服务器"+host+":"+port+"失败");
                    }
                }
            });
        }
    }

    //加入多播组
    private void joinMulticatGroup(){
        try {
            multicastSocket = new MulticastSocket(PORT);
            InetAddress inetAddress = InetAddress.getByName(IP);
            multicastSocket.joinGroup(inetAddress);

            mMulticastReceiveThread = new MulticastReceiveThread(multicastSocket);
            new Thread(mMulticastReceiveThread).start();

            mMulticastSendThread = new MulticastSendThread(multicastSocket);
            new Thread(mMulticastSendThread).start();

            System.out.println("成功加入多播组");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMulticastMsg(int code,Serializable value){
        if(mMulticastSendThread.open){
            mMulticastSendThread.send(code,gson.toJson(value));
        }
    }

    public void sendMulticastMsg(int code,String value){
        if(mMulticastSendThread.open){
            mMulticastSendThread.send(code,value);
        }
    }

    class MulticastSendThread implements Runnable{
        ArrayBlockingQueue<DatagramPacket> datas = new ArrayBlockingQueue<>(100);
        MulticastSocket mMulticastSocket;
        boolean open;

        public MulticastSendThread(MulticastSocket multicastSocket){
            mMulticastSocket = multicastSocket;
            open = true;
        }

        public void send(int code,String value){
            try {
                byte[] buffer = (code + (value==null?"":value)).getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length);
                datagramPacket.setAddress(InetAddress.getByName(IP));
                datagramPacket.setPort(PORT);
                datas.add(datagramPacket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        void close(){
            open = false;
        }

        @Override
        public void run() {
            while (open) {
                DatagramPacket data = null;
                try {
                    System.out.println("等待发送广播消息");
                    data = datas.take();
                    System.out.println("发送广播消息:"+new String(data.getData()));
                    mMulticastSocket.send(data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class MulticastReceiveThread implements Runnable{
        MulticastSocket mMulticastSocket;
        boolean open = false;

        public MulticastReceiveThread(MulticastSocket multicastSocket){
            mMulticastSocket = multicastSocket;
            open = true;
        }

        void close(){
            open = false;
        }

        @Override
        public void run() {
            while (open) {
                try {
                    byte[] buf = new byte[1024*8];
                    DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                    System.out.println("等待接收广播消息");
                    mMulticastSocket.receive(datagramPacket);
                    MsgParser.parse(datagramPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public TcpServer getTcpServer() {
        return mTcpServer;
    }

    public TcpClient getTcpClient() {
        return mTcpClient;
    }
}
