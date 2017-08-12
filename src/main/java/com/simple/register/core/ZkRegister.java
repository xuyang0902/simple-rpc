package com.simple.register.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.simple.common.RemotingHelper;
import com.simple.constant.RpcConstants;
import com.simple.register.RegisterConfig;
import com.simple.rpc.exception.RpcException;
import com.simple.rpc.model.RpcURL;
import com.simple.rpc.model.NodeConfig;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * zk注册中心
 *
 * @author xu.qiang
 * @date 17/8/10
 */
public class ZkRegister implements Register {


    /**
     * /yuren
     * /yuren/com.simple.rpc.test.App.1.0/provider/{application="test-app0provider",ip="192.168.124.7",ip="7777"}
     * /yuren/com.simple.rpc.test.App.1.0/consumer/{application="test-app-consumer",ip="192.168.124.7",ip="8888"}
     */

    private static final Logger logger = LoggerFactory.getLogger(ZkRegister.class);


    private Lock consumerLock = new ReentrantLock();

    /**
     * 消费端 缓存的注册中心的提供者集合信息
     * * key:rpcurl.toString()  服务名
     * value: 提供者主机信息集合
     */
    public static ConcurrentHashMap<String, List<NodeConfig>> consumerCachedProviderNodes = new ConcurrentHashMap<String, List<NodeConfig>>();

    //监听zk节点变化的处理线程
    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    /**
     * 以自己的花名给一个根目录吧  哈哈
     */
    private final static String DEFAULT_ROOT = "yuren";

    private CuratorFramework curatorFramework;

    private String zkAddress;

    public ZkRegister(RegisterConfig registerConfig) {

        if (!RpcConstants.ZOOKEEPER.equals(registerConfig.getProtocol())) {
            throw new IllegalArgumentException("expect register protocol zookeeper");
        }

        String zkAddr = registerConfig.getAddress();

        if (null == zkAddr || "".equals(zkAddr)) {
            throw new IllegalArgumentException("zk address can not be empty");
        }

        this.zkAddress = zkAddr;

        //1 重试策略：重试时间为0s 重试10次  [默认重试策略:无需等待一直抢，抢10次］
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(0, 10);

        //2 通过工厂创建连接
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(zkAddr)
                .sessionTimeoutMs(1000) //会话超时时间
                .retryPolicy(retryPolicy)
                .namespace(DEFAULT_ROOT)
                .build();
        //3 开启连接
        curatorFramework.start();
    }

    @Override
    public void doRegister(RpcURL rpcURL) {
        try {
            String serverUrl = "/" + rpcURL.getServicePath();
            Stat stat = curatorFramework.checkExists().forPath(serverUrl);
            if (stat == null) {
                //服务目录不存在 创建
                curatorFramework.create().forPath(serverUrl);
            }


            NodeConfig nodeConfig = this.getNodeConfig(rpcURL);
            String providersPath = serverUrl + "/providers/" + nodeConfig;
            Stat providerStat = curatorFramework.checkExists().forPath(providersPath);
            if (providerStat == null) {
                //服务目录不存在 创建
                curatorFramework.createContainers(providersPath);
            }

        } catch (Exception e) {
            logger.error("ZookeeperRegister.doRegister error rpcUrl:" + JSON.toJSONString(rpcURL));
        }
    }

    @Override
    public void doUnregister(RpcURL rpcURL) {
        try {
            String serverUrl = "/" + rpcURL.getServicePath();
            NodeConfig nodeConfig = this.getNodeConfig(rpcURL);
            curatorFramework.delete().forPath(serverUrl + "/providers/" + nodeConfig);

        } catch (Exception e) {
            logger.error("ZookeeperRegister.doRegister error ", e);
        }
    }

    @Override
    public void doSubscribe(RpcURL rpcURL) {

        try {

            String serverUrl = "/" + rpcURL.getServicePath();
            Stat stat = curatorFramework.checkExists().forPath(serverUrl);
            if (stat == null) {
                //服务目录不存在 创建
                curatorFramework.create().forPath(serverUrl);
            }

            NodeConfig nodeConfig = this.getNodeConfig(rpcURL);
            String consumersPath = serverUrl + "/consumers/" + nodeConfig;
            Stat consumersStat = curatorFramework.checkExists().forPath(consumersPath);
            if (consumersStat == null) {
                //服务目录不存在 创建
                curatorFramework.createContainers(consumersPath);
            }

            //检查提供者
            Boolean needCheck = Boolean.valueOf(String.valueOf(rpcURL.getAttr("checkProviders")));

            /**
             * 拿到这个服务下providers下的信息
             */
            String providerPath = serverUrl + "/providers";
            List<String> strings = curatorFramework.getChildren().forPath(providerPath);

            if (needCheck && CollectionUtils.isEmpty(strings)) {
                throw new RpcException("no providrs ：" + rpcURL.getServicePath());
            }

            final String servicePath = rpcURL.getServicePath();
            for (String kid : strings) {
                addConsumerCachedProviderNodes(servicePath, kid);
            }

            PathChildrenCache childrenCache = new PathChildrenCache(curatorFramework, providerPath, false);
            PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    ChildData data = event.getData();
                    String path = data.getPath();
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            addConsumerCachedProviderNodes(servicePath, path);
                            System.out.println("CHILD_ADDED : " + data.getPath() + "  数据:" + data.getData());
                            break;
                        case CHILD_REMOVED:
                            removeConsumerCachedProviderNodes(servicePath, path);
                            System.out.println("CHILD_REMOVED : " + data.getPath() + "  数据:" + data.getData());
                            break;
                        case CHILD_UPDATED:
                            System.out.println("CHILD_UPDATED : " + data.getPath() + "  数据:" + data.getData());
                            break;
                        default:
                            break;
                    }
                }
            };
            childrenCache.getListenable().addListener(childrenCacheListener, executorService);
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        } catch (Exception e) {
            logger.error("ZookeeperRegister.doSubscribe watch data change error ", e);
        }


    }

    @Override
    public void doUnSubscribe(RpcURL rpcURL) {

        try {
            String serverUrl = "/" + rpcURL.getServicePath();
            NodeConfig nodeConfig = this.getNodeConfig(rpcURL);
            curatorFramework.delete().forPath(serverUrl + "/consumers/" + nodeConfig);

        } catch (Exception e) {
            logger.error("ZookeeperRegister.doUnSubscribe error ", e);
        }


    }

    public String getZkAddress() {
        return zkAddress;
    }


    /**
     * 添加消费端 接口提供者节点信息
     *
     * @param serverPath   接口信息
     * @param providerNode
     */
    private void addConsumerCachedProviderNodes(String serverPath, String providerNode) {

        consumerLock.lock();

        try {
            List<NodeConfig> nodeConfigs = consumerCachedProviderNodes.get(serverPath);

            if (CollectionUtils.isEmpty(nodeConfigs)) {
                nodeConfigs = new ArrayList<NodeConfig>();
                consumerCachedProviderNodes.put(serverPath, nodeConfigs);
            }

            NodeConfig nodeConfig = JSONObject.parseObject(providerNode, NodeConfig.class);

            if (!nodeConfigs.contains(nodeConfig)) {
                nodeConfigs.add(nodeConfig);
            }

        } finally {
            consumerLock.unlock();
        }

    }

    /**
     * 移除消费端 缓存的提供者节点信息
     *
     * @param serverPath
     * @param providerNode
     */
    private void removeConsumerCachedProviderNodes(String serverPath, String providerNode) {
        consumerLock.lock();

        try {
            NodeConfig nodeConfig = JSONObject.parseObject(providerNode, NodeConfig.class);
            consumerCachedProviderNodes.get(serverPath).remove(nodeConfig);

        } finally {
            consumerLock.unlock();
        }
    }

    /**
     * 理论上这步拿到的nodeConfig信息应该是全的 以防万一 特殊处理
     *
     * @param rpcURL
     * @return
     */
    private NodeConfig getNodeConfig(RpcURL rpcURL) {

        String application = String.valueOf(rpcURL.getAttr("application"));
        String ip = String.valueOf(rpcURL.getAttr("ip"));
        Integer port = (Integer) rpcURL.getAttr("port");

        if (StringUtils.isEmpty(application)) {
            application = "defualt-app";
            logger.warn("use simple-rpc framework default name ");
        }
        if (StringUtils.isEmpty(ip)) {
            ip = RemotingHelper.getHostAddr();
        }
        if (port == null) {
            port = 7777;
            logger.warn("use simple-rpc framework default port 7777 ");
        }

        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setApplication(application);
        nodeConfig.setIp(ip);
        nodeConfig.setPort(port);

        return nodeConfig;

    }

}
