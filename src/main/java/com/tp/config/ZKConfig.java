package com.tp.config;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CountDownLatch;
@Configuration
public class ZKConfig {

    public static final String zookeeper_server = "10.2.38.4:2181";

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    @Bean
    public ZooKeeper connect(){
        ZooKeeper zooKeeper = null;

        try {
            zooKeeper = new ZooKeeper(zookeeper_server, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    Event.KeeperState state = watchedEvent.getState();
                    //连接成功
                    if (state == Event.KeeperState.SyncConnected) {
                        System.out.println("连接zookeeper成功...");
                        countDownLatch.countDown();
                    }else if (state == Event.KeeperState.Disconnected){
                        System.out.println("与zookeeper断开连接...");
                    }
                }
            });
            countDownLatch.await();
        }catch (Exception e){

        }
        return zooKeeper;
    }
}
