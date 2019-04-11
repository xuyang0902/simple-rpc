package com.xrpc.register;

/**
 * 註冊中心配置 默认zookeeper
 * @author xu.qiang
 * @date 17/8/11
 */
public class RegisterConfig {

    private String protocol = "zookeeper";

    //192.168.1.110:2181
    private String address;


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
