package com.simple.rpc.client;

/**
 * 远程服务
 *
 * @author xu.qiang
 * @date 17/8/11
 */
public class ReferBean {

    private String serviceName;
    private String version;

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

}
