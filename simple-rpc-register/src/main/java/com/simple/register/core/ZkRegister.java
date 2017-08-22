package com.simple.register.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.simple.commons.constant.RpcConstants;
import com.simple.commons.exception.RpcException;
import com.simple.commons.model.NodeURL;
import com.simple.commons.model.RpcURL;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.simple.register.RegisterConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * /yuren/com.simple.rpc.test.App/provider/{application="test-app0provider",ip="192.168.124.7",ip="7777",version="1.0"}
     * /yuren/com.simple.rpc.test.App/consumer/{application="test-app-consumer",ip="192.168.124.7",ip="8888"}
     */

    private static final Logger logger = LoggerFactory.getLogger(ZkRegister.class);


    private Lock consumerLock = new ReentrantLock();

    //服务端注册的服务
    public static ConcurrentHashMap<String, RpcURL> registerUrls = new ConcurrentHashMap<String, RpcURL>();
    //客户端订阅的服务
    public static ConcurrentHashMap<String, RpcURL> subscribeUrls = new ConcurrentHashMap<String, RpcURL>();

    /**
     * 消费端 缓存的注册中心的提供者集合信息
     * * key：  服务名
     * value: 提供者主机信息集合
     */
    public static ConcurrentHashMap<String, List<NodeURL>> consumerCachedProviderNodes = new ConcurrentHashMap<String, List<NodeURL>>();

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


        curatorFramework.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            public void stateChanged(CuratorFramework client, ConnectionState state) {
                if (state == ConnectionState.RECONNECTED) {
                    /**
                     * 因为消费端 提供端 都是用临时节点  当zk客户端重连的时候
                     * 提供者：已经注册的节点需要重新注册一遍
                     * 订阅者：已经订阅的服务  需要重新再订阅一遍
                     *
                     */

                    //已经注册的url 重新注册一遍
                    Set<Map.Entry<String, RpcURL>> entries = registerUrls.entrySet();
                    for (Map.Entry<String, RpcURL> entry : entries) {
                        //服务注册
                        doRegister(entry.getValue());
                    }

                    //已经订阅的url
                    Set<Map.Entry<String, RpcURL>> entries2 = subscribeUrls.entrySet();
                    for (Map.Entry<String, RpcURL> entry : entries2) {
                        try{
                            //订阅服务
                            initSubscribe(entry.getValue());
                        }catch (Exception e){
                            logger.warn("init subscribe when RECONNECTED faile e:",e);
                        }

                    }
                }
            }
        });

        //3 开启连接
        curatorFramework.start();
    }

    @Override
    public void doRegister(RpcURL rpcURL) {
        try {
            String application = String.valueOf(rpcURL.getAttr("application"));
            String ip = String.valueOf(rpcURL.getAttr("ip"));
            Integer port = (Integer) rpcURL.getAttr("port");

            if (StringUtils.isEmpty(application) || StringUtils.isEmpty(ip) || null == port) {
                throw new RuntimeException("application,ip,port in RPCURL attr can not be null when doRegister");
            }

            //接口服务节点
            String serverUrl = "/" + rpcURL.getServiceBean();
            creatZkPathIfNotExisted(serverUrl, true);

            //提供者节点
            String providersNodePath = serverUrl + "/providers";
            creatZkPathIfNotExisted(providersNodePath, true);

            //提供者url节点
            String providersUrl = providersNodePath + "/" + NodeURL.get(rpcURL);
            creatZkPathIfNotExisted(providersUrl, false);

            //注册的url
            if (!registerUrls.containsKey(rpcURL.toString())) {
                registerUrls.put(rpcURL.toString(), rpcURL);
            }

        } catch (Exception e) {
            logger.error("ZookeeperRegister.doRegister error rpcUrl:" + JSON.toJSONString(rpcURL));
        }
    }

    @Override
    public void doUnregister(RpcURL rpcURL) {
        try {
            String serverUrl = "/" + rpcURL.getServiceBean();
            curatorFramework.delete().forPath(serverUrl + "/providers/" + NodeURL.get(rpcURL));
        } catch (Exception e) {
            logger.warn("ZookeeperRegister.doRegister error",e);
        }
    }

    @Override
    public void doSubscribe(RpcURL rpcURL) {

        try {

            initSubscribe(rpcURL);

            final String providersNode = "/" + rpcURL.getServiceBean() + "/providers";
            //订阅者 监听提供节点消息
            final String serviceBean = rpcURL.getServiceBean();
            PathChildrenCache childrenCache = new PathChildrenCache(curatorFramework, providersNode, false);
            PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    ChildData data = event.getData();
                    if(data == null){
                        return;
                    }
                    String path = data.getPath();
                    if(path == null){
                        return;
                    }

                    switch (event.getType()) {
                        case CHILD_ADDED:
                            addConsumerCachedProviderNodes(serviceBean, path.substring(providersNode.length()+1));
                            logger.info(providersNode + " 节点下 ADD，path：{}，data：{}  ", data.getPath(), data.getData());
                            break;
                        case CHILD_REMOVED:
                            removeConsumerCachedProviderNodes(serviceBean, path.substring(providersNode.length()+1));
                            logger.info(providersNode + " 节点下 REMOVED，path：{}，data：{}  ", data.getPath(), data.getData());
                            break;
                        case CHILD_UPDATED:
                            logger.info(providersNode + " 节点下 UPDATED，path：{}，data：{}  ", data.getPath(), data.getData());
                            break;
                        default:
                            break;
                    }
                }
            };
            childrenCache.getListenable().addListener(childrenCacheListener, executorService);
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);


            //订阅的的url
            if(!subscribeUrls.containsKey(rpcURL.toString())){
                subscribeUrls.put(rpcURL.toString(), rpcURL);
            }

        } catch (Exception e) {
            logger.error("ZookeeperRegister.doSubscribe watch data change error ", e);
        }

    }


    /**
     * 初始化订阅
     * @param rpcURL
     */
    private void initSubscribe(RpcURL rpcURL) throws Exception {
        //服务节点
        String serverUrl = "/" + rpcURL.getServiceBean();
        creatZkPathIfNotExisted(serverUrl, true);

        //消费节点
        String consumersNode = serverUrl + "/consumers";
        creatZkPathIfNotExisted(consumersNode, true);

        //消费节点url
        String consumersPath = consumersNode + "/" + NodeURL.get(rpcURL);
        creatZkPathIfNotExisted(consumersPath, false);

        /**
         * 拿到这个服务下providers下的信息
         */
        final String providersNode = serverUrl + "/providers";
        List<String> providerPaths = curatorFramework.getChildren().forPath(providersNode);

        //检查提供者
        Boolean needCheck = Boolean.valueOf(String.valueOf(rpcURL.getAttr("checkProviders")));
        if (needCheck) {
            if (CollectionUtils.isEmpty(providerPaths)) {
                throw new RpcException("no providrs ：" + rpcURL.toString());
            }
            boolean noProvider = true;
            for (String path : providerPaths) {
                NodeURL url = JSON.parseObject(path, NodeURL.class);
                if (url.getVersion().equals(rpcURL.getVersion())) {
                    //存在提供者节点
                    noProvider = false;
                }
            }

            if (noProvider) {
                throw new RpcException("no providrs ：" + rpcURL.toString());
            }
        }

        //本地消费端缓存 提供者URL节点信息
        for (String providerRpcUrl : providerPaths) {
            NodeURL url = JSON.parseObject(providerRpcUrl, NodeURL.class);
            //只缓存 对应的上版本的提供者信息
            if (url.getVersion().equals(rpcURL.getVersion())) {
                addConsumerCachedProviderNodes(rpcURL.getServiceBean(), providerRpcUrl);
            }
        }
    }


    @Override
    public void doUnSubscribe(RpcURL rpcURL) {

        try {
            String serverUrl = "/" + rpcURL.getServiceBean();
            curatorFramework.delete().forPath(serverUrl + "/consumers/" + rpcURL.toString());

        } catch (Exception e) {
            logger.warn("ZookeeperRegister.doUnSubscribe error ", e);
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
            List<NodeURL> nodeInfos = consumerCachedProviderNodes.get(serverPath);

            if (CollectionUtils.isEmpty(nodeInfos)) {
                nodeInfos = new ArrayList<NodeURL>();
                consumerCachedProviderNodes.put(serverPath, nodeInfos);
            }


            NodeURL node = JSONObject.parseObject(providerNode, NodeURL.class);

            if(!nodeInfos.contains(node)){
                nodeInfos.add(node);
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
            RpcURL nodeConfig = JSONObject.parseObject(providerNode, RpcURL.class);
            consumerCachedProviderNodes.get(serverPath).remove(nodeConfig);

        }catch (Exception e){
            logger.error("remove provider node  when provider node is not active error",e);
            //再尝试一遍  一般不会到这步
            RpcURL nodeConfig = JSONObject.parseObject(providerNode, RpcURL.class);
            consumerCachedProviderNodes.get(serverPath).remove(nodeConfig);
        }finally {
            consumerLock.unlock();
        }
    }

    /**
     * 如果path在zk下目录下不存在 则创建 默认永久
     *
     * @param path       路径
     * @param persistent 是否永久节点
     * @throws Exception
     */
    private void creatZkPathIfNotExisted(String path, boolean persistent) throws Exception {
        Stat stat = curatorFramework.checkExists().forPath(path);
        if (stat == null) {
            //服务目录不存在 创建
            if (persistent) {
                curatorFramework.create().forPath(path);
            } else {
                curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(path);
            }

        }
    }
}
