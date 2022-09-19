package cn.mine;

import cn.mine.message.RpcResponseMessage;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Test {
    @org.junit.Test
    public void test() {
        try {
            String address = InetAddress.getLocalHost().getHostAddress();
            System.out.println(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
