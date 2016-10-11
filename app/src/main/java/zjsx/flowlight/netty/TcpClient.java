/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package zjsx.flowlight.netty;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;


public final class TcpClient {

    EventLoopGroup group = new NioEventLoopGroup();

    ChannelFuture lastWriteFuture = null;
    Channel mChannel;
    String mHost;
    int mPort;
    boolean connected;
    int mConnectTimes;
    boolean open;

    public boolean isConnected() {
        return open && connected;
    }

    public TcpClient(String host, int port) {
        mHost = host;
        mPort = port;
        open = true;
    }

    public interface ClientListener{
        void onConnectFinished(boolean reconnect,boolean success);
    }


    public void connect(final boolean reconnect,final boolean reset, final ClientListener clientListener) {
        if(open) {
            connected = false;
            if (reset) mConnectTimes = 0;
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new TcpClientInitializer(this, clientListener));
            mChannel = b.connect(mHost, mPort).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture future) throws Exception {
                    if (clientListener != null)
                        clientListener.onConnectFinished(reconnect, future.isSuccess());
                    if (future.isSuccess()) {
                        connected = true;
                        System.out.println("TCPClient connect success");
                    } else {
                        System.err.print("TCPClient connect failed");
                        final EventLoop loop = future.channel().eventLoop();
                        loop.schedule(new Runnable() {

                            @Override

                            public void run() {
                                if (mConnectTimes < 3) {
                                    mConnectTimes++;
                                    System.err.print("The " + mConnectTimes + " times reconnect");
                                    connect(reconnect, false, clientListener);
                                } else {
                                    //连接失败
                                }
                            }

                        }, 1L, TimeUnit.SECONDS);
                    }
                }
            }).channel();
        }
    }

    public void send(int code, String msg) {
        lastWriteFuture = mChannel.writeAndFlush(code + (msg == null?"":msg) + "\n");
    }

    public void disconnect() {
        open = false;
        try {
            if (mChannel != null) {
                mChannel.closeFuture();
            }
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
