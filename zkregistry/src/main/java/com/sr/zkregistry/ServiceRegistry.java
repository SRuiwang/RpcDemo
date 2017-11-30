package com.sr.zkregistry;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * 服务注册 ，ZK 在该架构中扮演了“服务注册表”的角色，用于注册所有服务器的地址与端口，并对客户端提供服务发现的功能
 */
public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private String registryAddress;

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    /**
     * @Authot: shaorui
     * @Description: 创建zk链接
     * @Date: 10:57 2017/11/29
     */
    public ZooKeeper connection() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, ZkConstants.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return zk;
    }


    /**
     * @Authot: shaorui
     * @Description: 服务注册
     * @Date: 11:02 2017/11/29
     */
    public void registry(String data) {
        if (data != null) {
            ZooKeeper zk = connection();
            try {
                createNode(zk, data);
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }

    /**
     * @Authot: shaorui
     * @Description: 创建服务节点
     * @Date: 11:03 2017/11/29
     */
    public void createNode(ZooKeeper zk, String data) throws KeeperException, InterruptedException {
        byte[] bytes = data.getBytes();
        if (zk.exists(ZkConstants.ZK_REGISTRY_PATH, null) == null) {
            zk.create(ZkConstants.ZK_REGISTRY_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        String s = zk.create(ZkConstants.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        LOGGER.debug("create zookeeper node ({} => {})", s, data);
    }

}