package com.tp.service.zookeeper;

public class DistributedSharedLockTest {
    public static void main(String[] args) {
        for (int i = 0; i < 20; i++) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    DistributedSharedLock lock = new DistributedSharedLock("/_locknode_");
                    try {
                        lock.acquire();
                        Thread.sleep(1000); //获得锁之后可以进行相应的处理
                        System.out.println("======获得锁后进行相应的操作======");
                        lock.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
    }
}
