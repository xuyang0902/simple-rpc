package com.xrpc.core.server;

import com.xprc.commons.model.NodeConfig;
import com.xprc.commons.model.RpcURL;
import com.xrpc.core.BootManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author xu.qiang
 * @date 17/8/19
 */
public class ServiceBean<T> implements  ApplicationContextAware, InitializingBean, DisposableBean {

    private ApplicationContext ctx;

    private String serviceName;
    private String version;

    // 接口实现类引用
    private T                   ref;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public T getRef() {
        return ref;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }

    @Override
    public void destroy() throws Exception {

//        NodeConfig nodeConfig = ctx.getBean(NodeConfig.class);
//
//        RpcURL rpcURL = new RpcURL();
//        rpcURL.setServiceBean(serviceName);
//        rpcURL.putAttr("application", nodeConfig.getApplication());
//        rpcURL.putAttr("ip", nodeConfig.getIp());
//        rpcURL.putAttr("port", nodeConfig.getPort());
//        rpcURL.setVersion(version);
//
//
//        BootManager.getHandlerMap().remove(rpcURL.getServiceBean());
//        BootManager.getLocalRegisterURLMap().remove(rpcURL.getServiceBean());
//        BootManager bootManager = ctx.getBean(BootManager.class);
//        bootManager.getZkRegister().doUnregister(rpcURL);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        NodeConfig nodeConfig = ctx.getBean(NodeConfig.class);

        RpcURL rpcURL = new RpcURL();
        rpcURL.setServiceBean(serviceName);
        rpcURL.putAttr("application", nodeConfig.getApplication());
        rpcURL.putAttr("ip", nodeConfig.getIp());
        rpcURL.putAttr("port", nodeConfig.getPort());
        rpcURL.setVersion(version);

        Object bean = ctx.getBean(serviceName);
        if (bean == null) {
            throw new RuntimeException(serviceName + " is not init in spring container");
        }

        BootManager.getHandlerMap().put(rpcURL.getServiceBean(), ref);
        BootManager.getLocalRegisterURLMap().put(rpcURL.getServiceBean(), rpcURL);
        BootManager bootManager = ctx.getBean(BootManager.class);
        bootManager.getZkRegister().doRegister(rpcURL);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
