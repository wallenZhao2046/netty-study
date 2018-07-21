package com.imooc.netty.ch12.thread;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@ChannelHandler.Sharable
public class ClientBusinessHandler extends SimpleChannelInboundHandler<ByteBuf> {
    public static final ChannelHandler INSTANCE = new ClientBusinessHandler();

    private static AtomicLong beginTime = new AtomicLong(0);
    private static AtomicLong totalResponseTime = new AtomicLong(0);
    private static AtomicInteger totalRequest = new AtomicInteger(0);

    // 一个单独的线程每两秒统计一次
    public static final Thread THREAD = new Thread(() -> {
        try {
            while (true) {
                long duration = System.currentTimeMillis() - beginTime.get();
                if (duration != 0) {
                    System.out.println("qps: " + 1000 * totalRequest.get() / duration + ", " + "avg response time: " + ((float) totalResponseTime.get()) / totalRequest.get());
                    Thread.sleep(2000);
                }
            }

        } catch (InterruptedException ignored) {
        }
    });

    // client 端每1秒发送给server端当前时间
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.executor().scheduleAtFixedRate(() -> {

            ByteBuf byteBuf = ctx.alloc().ioBuffer();
            byteBuf.writeLong(System.currentTimeMillis());
            ctx.channel().writeAndFlush(byteBuf);

        }, 0, 1, TimeUnit.SECONDS);
    }

    // client 端读取server端的返回用当前时间比较之前发送的时间, 得出从client端发送时 到 接收后的延迟时间
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        totalResponseTime.addAndGet(System.currentTimeMillis() - msg.readLong());
        totalRequest.incrementAndGet();

        // 如果是第一次 beginTime = 0, 则启动统计线程
        if (beginTime.compareAndSet(0, System.currentTimeMillis())) {
            THREAD.start();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // ignore
    }
}
