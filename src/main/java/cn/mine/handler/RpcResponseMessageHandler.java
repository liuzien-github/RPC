package cn.mine.handler;

import cn.mine.message.RpcResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    public static final Map<Integer, Promise<Object>> promises = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponseMessage rpcResponseMessage) throws Exception {
        Promise<Object> promise = promises.remove(rpcResponseMessage.getSequenceId());
        if (promise != null) {
            Object returnValue = rpcResponseMessage.getReturnValue();
            Exception exception = rpcResponseMessage.getException();
            if (exception == null) {
                promise.setSuccess(returnValue);
            } else {
                promise.setFailure(exception);
            }
        }
    }
}
