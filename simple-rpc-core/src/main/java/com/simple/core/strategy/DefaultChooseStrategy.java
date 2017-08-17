package com.simple.core.strategy;

import com.simple.commons.model.NodeURL;

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
    public NodeURL choose(List<NodeURL> nodes) {

        int index = auto.getAndIncrement() % nodes.size();

        return nodes.get(index);
    }
}
