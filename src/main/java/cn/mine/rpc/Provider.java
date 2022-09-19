package cn.mine.rpc;

import cn.mine.config.ConfigUtil;
import cn.mine.handler.RpcRequestMessageHandler;
import cn.mine.protocol.MessageCodecSharable;
import cn.mine.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Provider {
    private static RpcRequestMessageHandler handler = new RpcRequestMessageHandler();
    private static MessageCodecSharable messageCodec = new MessageCodecSharable();
    private static EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static EventLoopGroup workerGroup = new NioEventLoopGroup();
    private static EventLoopGroup handlerGroup = new DefaultEventLoopGroup();

    public static void start() {
        try {
            ConfigUtil.initService();
            new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline()
                                    .addLast(handlerGroup, new ProcotolFrameDecoder())
                                    .addLast(handlerGroup, messageCodec)
                                    .addLast(handlerGroup, handler);
                        }
                    })
                    .bind(ConfigUtil.getProviderPort());
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            handlerGroup.shutdownGracefully();
            ConfigUtil.stopZookeeperClient();
        }
    }

    public static void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        handlerGroup.shutdownGracefully();
        ConfigUtil.stopZookeeperClient();
    }
}
