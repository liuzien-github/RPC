package cn.mine.handler;

import cn.mine.config.ConfigUtil;
import cn.mine.message.RpcRequestMessage;
import cn.mine.message.RpcResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequestMessage message) throws Exception {
        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage();
        rpcResponseMessage.setSequenceId(message.getSequenceId());
        try {
            String implName = ConfigUtil.getImplName(message.getInterfaceName());
            Class<?> implClass = Class.forName(implName);
            Method method = implClass.getMethod(message.getMethodName(), message.getParameterTypes());
            Object o = method.invoke(message.getParameters());
            rpcResponseMessage.setReturnValue(o);
        } catch (Exception e) {
            rpcResponseMessage.setException(e);
        } finally {
            channelHandlerContext.channel().writeAndFlush(rpcResponseMessage);
        }
    }
}
