package com.simple.core.strategy;


import com.simple.commons.model.NodeURL;

import java.util.List;

/**
 * 选择策略
 *
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
    NodeURL choose(List<NodeURL> nodes);
}
