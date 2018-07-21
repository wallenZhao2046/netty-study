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
package com.imooc.netty.ch12.ws;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Echoes uppercase content of text frames.
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled

        if (frame instanceof TextWebSocketFrame) {
            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            logger.info("{} received {}", ctx.channel(), request);
            System.out.println("recv request: " + request);
            String result = getResult(request);

            ctx.channel().writeAndFlush(new TextWebSocketFrame(result));
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    protected String getResult(String data) {
        // 90.0% == 1ms
        // 95.0% == 10ms  1000 50 > 10ms
        // 99.0% == 100ms 1000 10 > 100ms
        // 99.9% == 1000ms1000 1 > 1000ms
        int level = ThreadLocalRandom.current().nextInt(1, 1000);

        int time;
        if (level <= 850) {
            time = 100;
        } else if (level <= 900) {
            time = 200;
        } else if (level <= 950) {
            time = 400;
        } else if (level <= 990) {
            time = 600;
        } else {
            time = 1000;
        }

        System.out.println("recv request: " + data);
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }

        Long now = new Date().getTime();
        System.out.println("return : " + data + " estimate: " + (now - new Long(data)));

        return data;
    }
}
