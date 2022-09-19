package cn.mine.message;

import java.util.Hashtable;
import java.util.Map;

public abstract class Message {
    protected static final int RPC_REQUEST_MESSAGE = 0;
    protected static final int RPC_RESPONSE_MESSAGE = 1;
    protected static final Map<Integer, Class<?>> messageClasses = new Hashtable<>();
    private int sequenceId;

    static {
        messageClasses.put(RPC_REQUEST_MESSAGE, RpcRequestMessage.class);
        messageClasses.put(RPC_RESPONSE_MESSAGE, RpcResponseMessage.class);
    }

    public static Class<?> getMessageClassByTypeId(int messageTypeId) {
        return messageClasses.get(messageTypeId);
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    public abstract int getMessageTypeId();
}
