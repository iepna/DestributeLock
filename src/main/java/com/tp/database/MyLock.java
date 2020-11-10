package com.tp.database;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.omg.CORBA.TIMEOUT;

import javax.sql.DataSource;
import javax.swing.text.StyledEditorKit;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MyLock {

    private static final String resourceName = "myresource";

    private static final Integer TIMEOUT = 10 * 1000;//超时10s

    private static final Integer STEPTIME = 100;//每100毫秒尝试获取一次锁

    private static final Integer RELEASETIME = 2*1000;//释放锁的时间

    private Long version = null;

    private String owner = null;

    /**
     * 获取锁
     */
    public void tryLock(){
        Integer timeOut = TIMEOUT;
        while (timeOut > 0){
            Long now = System.currentTimeMillis();
            version = now + RELEASETIME;
            Map<String,Object> result = Database.selectByResourceName(resourceName);
            if (result==null){
                //没有被获取锁，直接尝试获取锁
                String sql = "insert into resource(resource_name,share,version,`desc`,update_time) values('"+resourceName+"','1',"+version+",'获取了锁',now())";
                Integer flag = Database.insertOrUpdate(sql);
                if (flag > 0){
                    System.out.println("获取锁成功...，version = " + version);
                    return;
                }
            } else {
                //如果有线程获取锁，判断该锁是否超时
                Long oldVersion = (Long) result.get("version");
                if (now>=oldVersion){
                    //锁超时了，直接获得锁
                    String sql = "update resource set version = " + version + " where resource_name ='" + resourceName + "' and version = " + oldVersion;
                    Integer flag = Database.insertOrUpdate(sql);
                    if (flag>0){
                        System.out.println("获取锁（超时）成功，version = " + version);
                        return;
                    }
                }
            }

            try{
                Thread.sleep(STEPTIME);
            }catch (Exception e){
                e.printStackTrace();
            }

            timeOut = timeOut - STEPTIME;
        }
    }

    /**
     * 释放锁
     */
    public void unLock(){
        String sql = "update resource  set version = "+System.currentTimeMillis()+" where resource_name = '" + resourceName +"' and version = " + version;
        Integer flag = Database.insertOrUpdate(sql);
        if (flag > 0){
            System.out.println("释放锁成功，version = " + version);
        }else{
            System.out.println("释放锁失败，version = " + version);
        }
    }

    /**
     * 悲观锁
     */
    public void lock(){
        owner = UUID.randomUUID().toString().replace("-","");
        Database.lock(owner);
    }
}
