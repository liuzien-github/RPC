package cn.mine.config;

import cn.mine.protocol.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.*;

@Slf4j
public class ConfigUtil {
    private static CuratorFramework zkClient;
    private static Random random = new Random();
    private static String providerIp;
    private static int providerPort;
    private static Serializer.Algorithm serializerAlgorithm;
    private static Map<String, String> map = new Hashtable<>();

    static {
        try (InputStream inputStream = ConfigUtil.class.getClassLoader().getResourceAsStream("rpc.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);

            Set<Map.Entry<Object, Object>> entries = properties.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {
                String key = (String) entry.getKey();
                if (key.endsWith("service")) {
                    map.put(key, (String) entry.getValue());
                }
            }

            try {
                Integer port = Integer.parseInt(properties.getProperty("provider.port"));
                new InetSocketAddress(port);
                providerPort = port;
            } catch (Exception e) {
                log.error("provider端口号配置错误，将采用默认端口号：10086！");
                providerPort = 10086;
            }

            try {
                providerIp = properties.getProperty("provider.ip");
                new InetSocketAddress(providerIp, providerPort);
            } catch (Exception e) {
                log.error("providerIP地址配置错误，将采用默认IP地址：192.168.2.1！");
                providerIp = "192.168.2.1";
            }

            String sa = properties.getProperty("serializer.algorithm");
            if (sa == null || !sa.equals("jdk")) {
                serializerAlgorithm = Serializer.Algorithm.Json;
            } else {
                serializerAlgorithm = Serializer.Algorithm.Jdk;
            }

            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();

            String connectString = properties.getProperty("server.address");
            if (connectString != null) {
                builder = builder.connectString(connectString);
            }

            String sessionTimeoutMs = properties.getProperty("session.timeout.ms");
            if (sessionTimeoutMs != null) {
                builder = builder.sessionTimeoutMs(Integer.parseInt(sessionTimeoutMs));
            }

            String connectionTimeoutMs = properties.getProperty("connection.timeout.ms");
            if (connectionTimeoutMs != null) {
                builder = builder.connectionTimeoutMs(Integer.parseInt(connectionTimeoutMs));
            }

            String namespace = properties.getProperty("namespace");
            if (namespace != null) {
                builder = builder.namespace(namespace);
            }

            String baseSleepTimeMs = properties.getProperty("base.sleep.time.ms");
            String maxRetries = properties.getProperty("max.retries");
            if (baseSleepTimeMs != null && maxRetries != null) {
                builder = builder.retryPolicy(new ExponentialBackoffRetry(Integer.parseInt(baseSleepTimeMs), Integer.parseInt(maxRetries)));
            }

            zkClient = builder.build();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static String getImplName(String interfaceName) {
        return map.get(interfaceName);
    }

    public static Serializer.Algorithm getSerializerAlgorithm() {
        return serializerAlgorithm;
    }

    public static int getProviderPort() {
        return providerPort;
    }

    public static String getProviderIp() {
        return providerIp;
    }

    public static void stopZookeeperClient() {
        zkClient.close();
    }

    public static InetSocketAddress getServerInetSocketAddress(String interfaceName) throws Exception {
        List<String> strings;
        try {
            strings = zkClient.getChildren().forPath("/" + interfaceName);
        } catch (Exception e) {
            throw new Exception("该服务接口没有注册服务主机节点！");
        }
        if (strings == null || strings.size() == 0) {
            throw new Exception("该服务接口没有注册服务主机节点！");
        }

        String node = strings.get(random.nextInt(strings.size()));
        byte[] bytes = zkClient.getData().forPath("/" + interfaceName + "/" + node);

        if (bytes == null || bytes.length == 0) {
            throw new Exception("该服务接口注册的主机节点缺少IP地址和端口号！");
        }
        String address = new String(bytes);
        String[] str = address.split(":");
        if (str == null || str.length != 2) {
            throw new Exception("该服务接口注册的主机节点的IP地址和端口号表示格式不正确！");
        }

        return new InetSocketAddress(str[0], Integer.parseInt(str[1]));
    }

    public static void initService() throws Exception {
        zkClient.start();
        Set<Map.Entry<String, String>> entries = map.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            registerServiceNode(entry.getKey(), entry.getValue());
        }
    }

    private static void registerServiceNode(String interfaceName, String implName) throws Exception {
        try {
            byte[] bytes = zkClient.getData().forPath("/" + interfaceName);
            map.put(interfaceName, new String(bytes));
        } catch (Exception e) {
            zkClient.create().forPath("/" + interfaceName, implName.getBytes());
        }

        zkClient.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath("/" + interfaceName + "/provider",
                (providerIp + ":" + providerPort).getBytes());

        NodeCache nodeCache = new NodeCache(zkClient, "/" + interfaceName);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                byte[] bytes = nodeCache.getCurrentData().getData();
                map.put(interfaceName, new String(bytes));
            }
        });
        nodeCache.start(true);
    }
}
