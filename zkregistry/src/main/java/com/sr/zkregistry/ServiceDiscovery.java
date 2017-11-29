package com.sr.zkregistry;


import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * 本类用于client发现server节点的变化 ，实现负载均衡
 */
public class ServiceDiscovery {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<String>();

    private String registryAddress;


    /**
     * zk链接
     *
     * @param registryAddress
     */
    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        ZooKeeper zk = connectServer();
        if (zk != null) {
            watchNode(zk);
        }
    }

    /**
     * 发现新节点
     *
     * @return
     */
    public String discover() {
        String data = null;
        if (dataList.size() == 1) {
            data = dataList.get(0);
            LOGGER.debug("using only data: {}", data);
        } else {
            data = dataList.get(new Random().nextInt(dataList.size()));
            LOGGER.debug("using random data: {}", data);
        }
        return data;
    }


    /**
     * 创建服务器连接
     *
     * @return
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, ZkConstants.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return zk;
    }

    /**
     * 监听
     *
     * @param zk
     */
    private void watchNode(final ZooKeeper zk) {
        try {
            // 获取所有子节点
            List<String> children = zk.getChildren(ZkConstants.ZK_REGISTRY_PATH, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zk);
                    }
                }
            });
            // 循环子节点
            List<String> list = new ArrayList<String>();
            for (String node : children) {
                byte[] data = zk.getData(ZkConstants.ZK_REGISTRY_PATH + "/" + node, false, null);
                list.add(new String(data));
            }
            LOGGER.debug("node data:{}", list);
            this.dataList = list;
            // 将节点信息记录在成员变量
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
}