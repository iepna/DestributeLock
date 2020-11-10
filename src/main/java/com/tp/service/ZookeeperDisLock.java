package com.tp.service;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
@Component
public class ZookeeperDisLock {

    @Autowired
    private ZooKeeper zooKeeper;

    static final String lockPath = "/my_lock";

    private String uuid = null;

    @PostConstruct
    public void init(){
        try {
            Stat stat = zooKeeper.exists(lockPath,true);
            if (stat==null) {
                zooKeeper.create(lockPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //加锁
    public void tryLock(){
        uuid = UUID.randomUUID().toString();
        try {
            //1.创建临时节点
            zooKeeper.create(lockPath + "/" + uuid,null, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
            //2.获取所有的临时节点
            List<String> children = zooKeeper.getChildren(lockPath,true);

            if (children.get(0).equals(uuid)){
                //是第一个，获取锁成功
                System.out.println("本节点是第一个节点，获取锁成功，uuid = " + uuid);
                return;
            }

            //3.监听上一个节点是否删除
            zooKeeper.register(new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    System.out.println("监听");
                    if (Event.EventType.NodeChildrenChanged.equals(watchedEvent.getState())){
                        System.out.println("节点删除了。。。可以获取锁了");
                    }
                }
            });

        }catch (Exception E){

        }
    }

    //解锁
    public void releaseLock(){
        try{
            zooKeeper.delete(lockPath+"/"+uuid,1);
            System.out.println("删除锁成功，uuid = " + uuid);
        }catch (Exception e){

        }
    }
}
