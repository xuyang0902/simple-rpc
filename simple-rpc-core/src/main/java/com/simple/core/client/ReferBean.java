package com.simple.core.client;

import com.simple.commons.model.NodeConfig;
import com.simple.commons.model.RpcURL;
import com.simple.core.BootClient;
import com.simple.core.BootManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author xu.qiang
 * @date 17/8/11
 */
public class ReferBean<T> implements FactoryBean, ApplicationContextAware, InitializingBean, DisposableBean {

    private Logger logger = LoggerFactory.getLogger(ReferBean.class);

    private ApplicationContext ctx;

    private String id;
    private String serviceName;
    private String version;

    private Class<?>             interfaceClass;

    // 接口代理类引用
    private transient volatile T ref;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    @Override
    public Object getObject() throws Exception {

        if (ref == null) {

            interfaceClass = getInterfaceClass();

            //创建代理对象
            Object o = BootClient.create(getObjectType(), version);
            this.ref = (T) o;

        }

        return ref;
    }

    @Override
    public void destroy() throws Exception {
//        NodeConfig nodeConfig = ctx.getBean(NodeConfig.class.getName(),NodeConfig.class);
//
//        RpcURL rpcURL = new RpcURL();
//        rpcURL.setServiceBean(serviceName);
//        rpcURL.putAttr("application", nodeConfig.getApplication());
//        rpcURL.putAttr("ip", nodeConfig.getIp());
//        rpcURL.putAttr("port", nodeConfig.getPort());
//        rpcURL.setVersion(version);
//
//        //取消订阅
//        BootManager bootManager = ctx.getBean(BootManager.class);
//        bootManager.getZkRegister().doUnSubscribe(rpcURL);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        NodeConfig nodeConfig = ctx.getBean(NodeConfig.class.getName(),NodeConfig.class);
        RpcURL rpcURL = new RpcURL();
        rpcURL.setServiceBean(serviceName);
        rpcURL.putAttr("application", nodeConfig.getApplication());
        rpcURL.putAttr("ip", nodeConfig.getIp());
        rpcURL.putAttr("port", nodeConfig.getPort());
        rpcURL.setVersion(version);
        BootManager bootManager = ctx.getBean(BootManager.class);


        //订阅服务
        bootManager.getZkRegister().doSubscribe(rpcURL);

        if(ref == null){
            getObject();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    @Override
    public Class<?> getObjectType() {
        return getInterfaceClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    public Class<?> getInterfaceClass() {
        if (interfaceClass != null) {
            return interfaceClass;
        }
        try {
            if (serviceName != null && serviceName.length() > 0) {
                this.interfaceClass = Class.forName(serviceName, true, Thread.currentThread()
                        .getContextClassLoader());
            }
        } catch (ClassNotFoundException t) {
            throw new IllegalStateException(t.getMessage(), t);
        }
        return interfaceClass;
    }
}
