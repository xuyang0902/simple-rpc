package com.simple;

import com.simple.rpc.client.ReferBean;
import com.simple.common.RemotingHelper;
import com.simple.register.RegisterConfig;
import com.simple.register.core.ZkRegister;
import com.simple.rpc.exception.RpcException;
import com.simple.rpc.model.RpcURL;
import com.simple.rpc.server.NettyRpcServer;
import com.simple.rpc.model.NodeConfig;
import com.simple.rpc.server.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xu.qiang
 * @date 17/8/11
 */
public class BootServer implements ApplicationContextAware, InitializingBean, DisposableBean {

    /**
     * rpc提供者map
     * key : 接口全类名+版本号
     * value : 实例化对象
     */
    private static ConcurrentHashMap<String, Object> handlerMap = new ConcurrentHashMap<String, Object>();

    /**
     * 本地注册rpcUrl的缓存
     */
    private static ConcurrentHashMap<String, RpcURL> localRegisterURLMap = new ConcurrentHashMap<String, RpcURL>();


    private RegisterConfig registerConfig;
    private NodeConfig nodeConfig;

    private ZkRegister zkRegister;

    private NettyRpcServer nettyRpcServer;

    private ApplicationContext ctx;

    private List<ReferBean> referBeans;

    private boolean consumerOnly = false;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }


    @Override
    public void afterPropertiesSet() throws Exception {

        if (registerConfig == null || StringUtils.isEmpty(registerConfig.getAddress())) {
            throw new RpcException("registerConfig can not be null");
        }


        if (nodeConfig == null) {
            throw new RpcException("registerConfig can not be null");
        }
        if (nodeConfig.getPort() <= 0) {
            nodeConfig.setPort(7777);
        }
        if (StringUtils.isEmpty(nodeConfig.getIp())) {
            nodeConfig.setIp(RemotingHelper.getHostAddr());
        }
        //01.初始化注册中心 服务端启动
        zkRegister = new ZkRegister(registerConfig);

        //02.将本地rpc服务注册到zk
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (!CollectionUtils.isEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                String verison = serviceBean.getClass().getAnnotation(RpcService.class).version();

                RpcURL rpcURL = new RpcURL();
                rpcURL.setServiceBean(interfaceName);
                rpcURL.putAttr("application", nodeConfig.getApplication());
                rpcURL.putAttr("ip", nodeConfig.getIp());
                rpcURL.putAttr("port", nodeConfig.getPort());
                rpcURL.setVersion(verison);

                //提供者缓存 服务接口 和 本地实例
                handlerMap.put(rpcURL.getServicePath(), serviceBean);

                /**
                 * 本地缓存 本机注册到服务器上的url
                 */
                localRegisterURLMap.put(rpcURL.getServicePath(), rpcURL);
                //将服务发布到注册中心
                zkRegister.doRegister(rpcURL);
            }
        }

        //03.消费者订阅远程服务
        if(referBeans != null && referBeans.size() > 0){
            for (ReferBean referBean : referBeans) {

                RpcURL rpcURL = new RpcURL();
                rpcURL.setServiceBean(referBean.getServiceName());
                rpcURL.putAttr("application", nodeConfig.getApplication());
                rpcURL.putAttr("ip", nodeConfig.getIp());
                rpcURL.putAttr("port", nodeConfig.getPort());
                rpcURL.setVersion(referBean.getVersion());

                zkRegister.doSubscribe(rpcURL);
            }
        }


        nettyRpcServer = new NettyRpcServer(nodeConfig);
        nettyRpcServer.start();

    }


    @Override
    public void destroy() throws Exception {
        for (Map.Entry<String, RpcURL> entry : localRegisterURLMap.entrySet()) {
            zkRegister.doUnregister(entry.getValue());
        }
    }

    public RegisterConfig getRegisterConfig() {
        return registerConfig;
    }

    public void setRegisterConfig(RegisterConfig registerConfig) {
        this.registerConfig = registerConfig;
    }

    public NodeConfig getNodeConfig() {
        return nodeConfig;
    }

    public void setNodeConfig(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    public List<ReferBean> getReferBeans() {
        return referBeans;
    }

    public void setReferBeans(List<ReferBean> referBeans) {
        this.referBeans = referBeans;
    }

    public boolean isConsumerOnly() {
        return consumerOnly;
    }

    public void setConsumerOnly(boolean consumerOnly) {
        this.consumerOnly = consumerOnly;
    }

    public static ConcurrentHashMap<String, Object> getHandlerMap() {
        return handlerMap;
    }


}
