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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


public final class TcpServer {

    private boolean launched;

    public static final int PORT = Integer.parseInt(System.getProperty("port", "8992"));
    ServerBootstrap mBootstrap;
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;

    Channel mChannel;

    public boolean isLaunched() {
        return launched;
    }

    public interface ServerListener{
        void onLaunchFinished(boolean success);
    }

    public void startService(final ServerListener serverListener){
        mBootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        mBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new TcpServerInitializer());
        mChannel = mBootstrap.bind(PORT).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(serverListener != null) serverListener.onLaunchFinished(future.isSuccess());
                if(future.isSuccess()){
                    launched = true;
                    System.out.println("TCPServer Launch Success");
                }else{
                    System.err.print("TCPServer Launch Failed");
                }
            }
        }).channel();
    }

    public void send(int code, String msg) {
        // Sends the received line to the server.
        for (Channel c: TcpServerHandler.channels) {
            System.out.println("向客户端"+c.remoteAddress()+"发送消息:"+code+msg);
            c.writeAndFlush(code+(msg == null?"":msg)+'\n');
        }
    }

    public void shutDown(){
        if(mChannel!=null){
            launched = false;
            mChannel.closeFuture();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
