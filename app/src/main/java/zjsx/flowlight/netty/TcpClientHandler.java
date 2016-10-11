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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import zjsx.flowlight.msg.MsgParser;

/**
 * Handles a client-side channel.
 */
public class TcpClientHandler extends SimpleChannelInboundHandler<String> {

    TcpClient mClient;
    TcpClient.ClientListener mClientListener;
    public TcpClientHandler(TcpClient client, TcpClient.ClientListener clientListener){
        mClient = client;
        mClientListener = clientListener;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        MsgParser.parse(ctx,msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        System.out.println("客户端链接断开");

        final EventLoop eventLoop = ctx.channel().eventLoop();

        eventLoop.schedule(new Runnable() {

            @Override

            public void run() {

                mClient.connect(true,true,mClientListener);

            }

        }, 1L, TimeUnit.SECONDS);

        super.channelInactive(ctx);

    }
}
