package com.tp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ZooleeperLockService {
    @Autowired
    private ZookeeperDisLock zookeeperDisLock;

    @PostConstruct
    public void done(){
        for(int i=0;i<10;i++){
            MyThread myThread = new MyThread();
            myThread.start();
        }

    }

    public class MyThread extends Thread{
        @Override
        public void run() {
            zookeeperDisLock.tryLock();
            zookeeperDisLock.releaseLock();
        }
    }
}
