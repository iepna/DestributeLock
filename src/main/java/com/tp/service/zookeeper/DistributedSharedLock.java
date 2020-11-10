package com.tp.service.zookeeper;

import io.netty.buffer.ByteBuf;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

public class DistributedSharedLock implements Watcher {

    private static final String ADDR = "10.2.38.4:2181";
    private static final String LOCK_NODE = "guid-lock-";
    private String rootLockNode;//锁目录
    private ZooKeeper zk = null;
    private Integer mutex;
    private Integer currentLock;

    public DistributedSharedLock(String rootLockNode){
        this.rootLockNode = rootLockNode;
        try{
            zk = new ZooKeeper(ADDR,10*10000,this);
        }catch (Exception e){
            e.printStackTrace();
        }
        mutex = new Integer(-1);

        //Create zk node Name
        if (zk != null){
            try{
                Stat stat = zk.exists(rootLockNode,true);

                if (stat==null){
                    zk.create(rootLockNode,new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    /**
     * 获取锁
     * @throws Exception
     */
    public void acquire()throws Exception{
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byte[] value;
        byteBuffer.putInt(ThreadLocalRandom.current().nextInt(10));
        value = byteBuffer.array();

        //创建锁节点
        String lockName = zk.create(rootLockNode + "/" + LOCK_NODE,value,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);

        synchronized (mutex){
            while (true){
                //获取当前锁节点number，和所有锁节点比较
                Integer acquireLock = new Integer(lockName.substring(lockName.lastIndexOf('-')+1));
                List<String> childLockNode = zk.getChildren(rootLockNode,true);

                SortedSet<Integer> sortedSet = new TreeSet<>();
                for (String temp:childLockNode){
                    Integer tempLockNumber = new Integer(temp.substring(temp.lastIndexOf('-')+1));
                    sortedSet.add(tempLockNumber);
                }

                currentLock = sortedSet.first();

                //如果当前创建的锁的序号是最小的那么认为这个客户端获得了锁
                if (currentLock>=acquireLock){
                    System.out.println("获取了锁，lockName="+lockName);
                    return;
                }else{
                    //没有获得锁则等待下次事件的发生
                    mutex.wait();
                }
            }
        }

    }

    /**
     * 释放锁
     * @throws Exception
     */
    public void release() throws Exception {
        String lockName = String.format("%010d",currentLock);
        lockName = rootLockNode + "/" + LOCK_NODE + lockName;
        zk.delete(lockName,-1);
        System.out.println("释放锁完成，lockName = " + lockName);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        synchronized (mutex){
            mutex.notifyAll();
        }
    }
}
