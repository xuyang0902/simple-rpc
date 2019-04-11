package com.xprc.commons.model;


import com.alibaba.fastjson.JSON;
import com.xprc.commons.constant.RpcConstants;
import com.xprc.commons.util.RemotingHelper;
import org.springframework.util.StringUtils;

/**
 * 应用节点配置
 *
 * @author xu.qiang
 * @date 17/8/10
 */
public class NodeConfig {

    //应用名称
    private String application;
    //节点ip地址
    private String ip;
    //节点rpc通信端口
    private int port;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getIp() {
        if (StringUtils.isEmpty(this.ip)) {
            this.ip = RemotingHelper.getHostAddr();
        }
        return this.ip;
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

    public String getAddress() {
        return ip + RpcConstants.COLON + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeConfig node = (NodeConfig) o;

        if (port != node.port) return false;
        return ip != null ? ip.equals(node.ip) : node.ip == null;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
