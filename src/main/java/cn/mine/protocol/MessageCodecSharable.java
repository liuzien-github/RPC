package cn.mine.protocol;

import cn.mine.config.ConfigUtil;
import cn.mine.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, List<Object> list) throws Exception {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeBytes(new byte[]{1, 2, 3, 4}); //魔数
        byteBuf.writeByte(1); //版本号
        byteBuf.writeByte(ConfigUtil.getSerializerAlgorithm().ordinal()); //序列化方式：JDK0，JSON1
        byteBuf.writeByte(message.getMessageTypeId());//消息类型
        byteBuf.writeInt(message.getSequenceId());//消息顺序号
        byteBuf.writeByte(0xff);//用作填充

        byte[] bytes = ConfigUtil.getSerializerAlgorithm().serialize(message);
        byteBuf.writeInt(bytes.length);//数据长度
        byteBuf.writeBytes(bytes); //数据
        //16字节的定长，其它的是数据
        list.add(byteBuf);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int magicNum = byteBuf.readInt();
        byte version = byteBuf.readByte();
        byte serializerType = byteBuf.readByte();
        byte messageType = byteBuf.readByte();
        int sequenceId = byteBuf.readInt();
        byteBuf.readByte();
        int length = byteBuf.readInt();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializerType];
        Class<?> messageClass = Message.getMessageClassByTypeId(messageType);
        Object message = algorithm.deserialize(messageClass, bytes);
        list.add(message);
    }
}
