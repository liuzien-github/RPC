package cn.mine.protocol;

import com.alibaba.fastjson.JSON;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface Serializer {
    <T> T deserialize(Class<T> clazz, byte[] bytes);
    <T> byte[] serialize(T object);

    enum Algorithm implements Serializer {
        Jdk {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try {
                    ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    return (T) oin.readObject();
                } catch (Exception e) {
                    throw new RuntimeException("反序列化失败", e);
                }
            }

            @Override
            public <T> byte[] serialize(T object) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(object);
                    return bos.toByteArray();
                } catch (Exception e) {
                    throw new RuntimeException("序列化失败", e);
                }
            }
        },
        Json {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                return JSON.parseObject(new String(bytes), clazz);
            }

            @Override
            public <T> byte[] serialize(T object) {
                return JSON.toJSONString(object).getBytes();
            }
        }
    }
}
