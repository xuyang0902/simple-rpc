package com.xprc.commons.model;

import com.alibaba.fastjson.JSON;
import com.xprc.commons.constant.RpcConstants;

import java.io.Serializable;

/**
 * 提供者 消费者 zk节点信息
 *
 * 仅仅保存 提供者 | 消费者的 通信信息  服务信息
 * @author xu.qiang
 * @date 17/8/17
 */
public class NodeURL implements Serializable{

    private static final long serialVersionUID = 2709801303637960704L;
    //应用名称
    private String application;
    //节点ip地址
    private String ip;
    //节点rpc通信端口
    private int port;
    //服务
    private String serviceBean;
    //版本号
    private String version;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServiceBean() {
        return serviceBean;
    }

    public void setServiceBean(String serviceBean) {
        this.serviceBean = serviceBean;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    /**
     * 获取zkpath信息
     * @param rpcURL
     * @return
     */
    public static NodeURL get(RpcURL rpcURL){
        String application = String.valueOf(rpcURL.getAttr("application"));
        String ip = String.valueOf(rpcURL.getAttr("ip"));
        Integer port = (Integer) rpcURL.getAttr("port");
        String serviceBean = rpcURL.getServiceBean();
        String version = rpcURL.getVersion();
        NodeURL zkPathInfo = new NodeURL();
        zkPathInfo.setApplication(application);
        zkPathInfo.setIp(ip);
        zkPathInfo.setPort(port);
        zkPathInfo.setServiceBean(serviceBean);
        zkPathInfo.setVersion(version);
        return zkPathInfo;
    }


    public String getAddress() {
        return ip + RpcConstants.COLON + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeURL nodeURL = (NodeURL) o;

        if (port != nodeURL.port) return false;
        if (application != null ? !application.equals(nodeURL.application) : nodeURL.application != null) return false;
        if (ip != null ? !ip.equals(nodeURL.ip) : nodeURL.ip != null) return false;
        if (serviceBean != null ? !serviceBean.equals(nodeURL.serviceBean) : nodeURL.serviceBean != null) return false;
        return version != null ? version.equals(nodeURL.version) : nodeURL.version == null;
    }

    @Override
    public int hashCode() {
        int result = application != null ? application.hashCode() : 0;
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (serviceBean != null ? serviceBean.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
