package cn.mine.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RpcRequestMessage extends Message {
    private String interfaceName;
    private String methodName;
    private Class[] parameterTypes;
    private Object[] parameters;

    @Override
    public int getMessageTypeId() {
        return RPC_REQUEST_MESSAGE;
    }
}
