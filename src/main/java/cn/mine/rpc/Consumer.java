package cn.mine.rpc;

import cn.mine.config.ConfigUtil;
import cn.mine.handler.RpcResponseMessageHandler;
import cn.mine.message.RpcRequestMessage;
import cn.mine.protocol.MessageCodecSharable;
import cn.mine.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer {
    private static AtomicInteger id = new AtomicInteger();
    private static MessageCodecSharable messageCodec = new MessageCodecSharable();
    private static RpcResponseMessageHandler handler = new RpcResponseMessageHandler();

    public static Object call(String interfaceName, String methodName, Class[] parameterTypes, Object[] parameters) throws Exception {
        RpcRequestMessage message = new RpcRequestMessage(interfaceName, methodName, parameterTypes, parameters);
        message.setSequenceId(id.getAndIncrement());

        DefaultEventLoop defaultEventLoop = new DefaultEventLoop();
        DefaultPromise<Object> promise = new DefaultPromise<>(defaultEventLoop);
        handler.promises.put(message.getSequenceId(), promise);

        InetSocketAddress socketAddress = ConfigUtil.getServerInetSocketAddress(interfaceName);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline()
                                .addLast(new ProcotolFrameDecoder())
                                .addLast(messageCodec)
                                .addLast(handler);
                    }
                })
                .connect(socketAddress);

        Channel channel = channelFuture.sync().channel();
        channel.writeAndFlush(message);

        promise.await();
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                workerGroup.shutdownGracefully();
                defaultEventLoop.shutdownGracefully();
            }
        });
        if (promise.isSuccess())
            return promise.getNow();
        else
            throw new Exception(promise.cause());
    }
}
