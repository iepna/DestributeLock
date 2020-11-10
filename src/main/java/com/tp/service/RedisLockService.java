package com.tp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class RedisLockService {
    @Autowired
    private RedisTemplate redisTemplate;

    private static final int DEFAULT_ACQUIRY_RESOLUTION_MILLIS = 100;
    //锁等待时间，防止线程饥饿
    private int timeoutMsecs = 5 * 1000;
    private volatile boolean locked = false;

    //获取键的值
    private String get(final String key) {
        Object obj = null;
        try {
            obj = redisTemplate.execute(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    StringRedisSerializer serializer = new StringRedisSerializer();
                    byte[] data = connection.get(serializer.serialize(key));
                    connection.close();
                    if (data == null) {
                        return null;
                    }
                    return serializer.deserialize(data);
                }
            });
        } catch (Exception e) {
            System.err.println("get redis error, key : " + key + "," + e.getMessage());
        }
        return obj != null ? obj.toString() : null;
    }

    //设置键的值
    public boolean setNX(final String key, final String value) {
        Object obj = null;
        try {
            obj = redisTemplate.execute(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    StringRedisSerializer serializer = new StringRedisSerializer();
                    Boolean success = connection.setNX(serializer.serialize(key), serializer.serialize(value));
                    connection.close();
                    return success;
                }
            });
        } catch (Exception e) {
            System.err.println("setNX redis error, key : " + key + "," + e.getMessage());
            e.printStackTrace();
        }

        return obj != null ? (Boolean) obj : false;
    }

    //获取并设置键的值，并返回旧值
    public String getSet(final String key, final String value) {
        Object obj = null;
        try {
            obj = redisTemplate.execute(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    StringRedisSerializer serializer = new StringRedisSerializer();
                    byte[] ret = connection.getSet(serializer.serialize(key), serializer.serialize(value));
                    connection.close();
                    return serializer.deserialize(ret);
                }
            });
        } catch (Exception e) {
            System.err.println("setNX redis error, key : " + key);
            e.printStackTrace();
        }
        return obj != null ? (String) obj : null;
    }

    //实现分布式锁
    public boolean lock(String lockKey, String lockValue) {
        int timeout = timeoutMsecs;
        while (timeout >= 0) {
            //long expires = System.currentTimeMillis() + expireMsecs + 1;
            //String expiresStr = String.valueOf(expires); //锁到期时间
            if (this.setNX(lockKey, lockValue)) {
                System.out.println("初次获取锁：lockKey = " + lockKey + ",lockValue = " + lockValue);
                // 获取锁
                locked = true;
                return true;
            }
            //redis系统的时间
            String currentValueStr = this.get(lockKey);
            //判断是否为空，不为空的情况下，如果被其他线程设置了值，则第二个条件判断是过不去的
            if (currentValueStr != null && Long.parseLong(currentValueStr) <= System.currentTimeMillis()) {

                //获取上一个锁到期时间，并设置现在的锁到期时间，
                //只有一个线程才能获取上一个线上的设置时间，因为jedis.getSet是同步的
                String oldValueStr = this.getSet(lockKey, lockValue);

                //防止误删（覆盖，因为key是相同的）了他人的锁——这里达不到效果，这里值会被覆盖，但是因为什么相差了很少的时间，所以可以接受
                if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
                    //分布式的情况下:如过这个时候，多个线程恰好都到了这里，但是只有一个线程的设置值和当前值相同，他才有权利获取锁
                    // 获取锁
                    System.out.println("过期获取锁：lockKey = " + lockKey + ",lockValue = " + lockValue);
                    locked = true;
                    return true;
                }
            }
            timeout -= DEFAULT_ACQUIRY_RESOLUTION_MILLIS;
            //延迟100 毫秒,
            try {
                Thread.sleep(DEFAULT_ACQUIRY_RESOLUTION_MILLIS);
            }catch (Exception e){
                System.err.println(lockKey + e.getMessage());
            }
        }
        return false;
    }
    
    //设置键的值
    private Long delete(final String key) {
        Object obj = null;
        try {
            obj = redisTemplate.execute(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    StringRedisSerializer serializer = new StringRedisSerializer();
                    //System.out.println(key + ":" + value);
                    Long success = connection.del(serializer.serialize(key));
                    connection.close();
                    return success;
                }
            });
        } catch (Exception e) {
            System.err.println("setNX redis error, key : " + key + "," + e.getMessage());
            e.printStackTrace();
        }
        return (Long)obj;
    }

    //释放锁
    public boolean releaseLock(String lockKey, String lockValue) {
        // 判断加锁与解锁是不是同一个客户端
        boolean flag = false;
        try{
            String value = get(lockKey);
            System.out.println(lockValue + ":" + value);
            if (lockValue.equals(value)){
                Long ret = delete(lockKey);
                System.out.println("释放锁成功，lockKey = " + lockKey + ", lockValue = " + lockValue);
                flag = true;
            }
        }catch (Exception e){
            System.err.println("删除锁出错" + e.getMessage());
        }
        return flag;
    }
}
