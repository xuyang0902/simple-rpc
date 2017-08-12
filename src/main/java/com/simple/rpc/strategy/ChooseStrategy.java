package com.simple.rpc.strategy;

import com.simple.rpc.model.NodeConfig;

import java.util.List;

/**
 * 选择策略
 * @author xu.qiang
 * @date 17/8/10
 */
public interface ChooseStrategy {


    /**
     * 选择提供者节点
     *
     * @param nodes
     * @return
     */
    NodeConfig choose(List<NodeConfig> nodes);
}
