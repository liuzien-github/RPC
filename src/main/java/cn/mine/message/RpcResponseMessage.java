package cn.mine.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RpcResponseMessage extends Message{
    private Object returnValue;
    private Exception exception;

    @Override
    public int getMessageTypeId() {
        return RPC_RESPONSE_MESSAGE;
    }
}
