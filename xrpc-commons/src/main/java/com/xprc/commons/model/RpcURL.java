package com.xprc.commons.model;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定义RPC调用过程的URL
 *
 * @author xu.qiang
 * @date 17/8/10
 */
public class RpcURL implements Serializable {

    private static final long serialVersionUID = -8204670277254591345L;

    private static AtomicInteger requestId = new AtomicInteger(1);

    //每次请求调用唯一url
    private int opaque = requestId.getAndIncrement();

    //服务
    private String serviceBean;
    //方法
    private String methodName;
    //方法参数类型
    private Class<?>[] parameterTypes;
    //参数
    private Object[] parameters;
    //版本号
    private String version;

    //url附属属性 比如ip port等
    private Map<String, Object> attrs;

    public String getServiceBean() {
        return serviceBean;
    }

    public void setServiceBean(String serviceBean) {
        this.serviceBean = serviceBean;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, Object> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    public void addAttr(String key, Object value) {
        this.attrs.put(key, value);
    }

    public Object getAttr(String key) {
        return this.attrs.get(key);
    }

    public Object putAttr(String key, Object value) {
        if(attrs == null){
            attrs= new ConcurrentHashMap<String, Object>(16);
        }
        return this.attrs.put(key, value);
    }

    public int getOpaque() {
        return opaque;
    }

    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RpcURL rpcURL = (RpcURL) o;

        if (serviceBean != null ? !serviceBean.equals(rpcURL.serviceBean) : rpcURL.serviceBean != null) return false;
        return version != null ? version.equals(rpcURL.version) : rpcURL.version == null;
    }

    @Override
    public int hashCode() {
        int result = serviceBean != null ? serviceBean.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}
