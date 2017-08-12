package com.simple.rpc.strategy;

import com.simple.rpc.model.NodeConfig;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 默认选择 一个一个轮训
 *
 * @author xu.qiang
 * @date 17/8/10
 */
public class DefaultChooseStrategy implements ChooseStrategy {


    private final AtomicInteger auto = new AtomicInteger(0);

    @Override
    public NodeConfig choose(List<NodeConfig> nodes) {

        int index = auto.getAndIncrement() % nodes.size();

        return nodes.get(index);

    }
}
