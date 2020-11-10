package com.tp.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class LockDemo {
    @Autowired
    private RedisLockService redisLockService;

    @PostConstruct
    public void lockDemo(){
        int i = 100;
        while (i > 0){
            i--;
            new Thread(){
                @Override
                public void run(){
                    String lockKey = "lockKey";
                    String lockValue = String.valueOf(System.currentTimeMillis() + 10000000);
                    
                    Boolean flag = redisLockService.lock(lockKey, lockValue);
                    if (!flag){
                        System.out.println("lockValue = " + lockValue + "lockValue = " + lockValue + "：过期未获取锁");
                        return;
                    }
                    try{
                        Thread.sleep(1000);
                        System.out.println(lockValue + "：处理业务");
                    }catch (Exception e){
                        System.err.println(lockKey + e.getMessage());
                    }
                    redisLockService.releaseLock(lockKey, lockValue);
                    
                }
            }.start();
            
            try{
                Thread.sleep(10);
            }catch (Exception e){
                
            }
        }
        
//        int i = 100;
//        while (i>0) {
//            i--;
//            String lockKey = "lockKey";
//            String lockValue = String.valueOf(System.currentTimeMillis() + 10000);
//            String value = redisLockService.getSet(lockKey, lockValue);
//            System.out.println("lockValue = " + lockValue + ", value = " + value);
//        }
    }

}
