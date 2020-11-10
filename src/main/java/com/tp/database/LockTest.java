package com.tp.database;

public class LockTest {
    public static void main(String[] args) {
        for (int i=0;i<100;i++){
            new Thread(){
                @Override
                public void run() {
                    MyLock myLock = new MyLock();
                    myLock.tryLock();
                    System.out.println("=======乐观锁处理业务=======");
                    myLock.unLock();
                }
            }.start();
        }

        /*for (int i=0;i<100;i++){
            new Thread(){
                @Override
                public void run() {
                    MyLock myLock = new MyLock();
                    myLock.lock();
                    //myLock.unLock();
                }
            }.start();
        }*/
    }
}
