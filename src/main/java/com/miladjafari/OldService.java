package com.miladjafari;

import com.miladjafari.helper.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Thread.sleep;

public class OldService {

    Map<String, String> map = new HashMap<>();
    ReadWriteLock lock = new ReentrantReadWriteLock();

    public void startBatchTransaction() {
        lock.writeLock().lock();
    }

    public void uploadPrice() {
        try {
            System.out.println("Write lock is available" + lock.writeLock().tryLock());


            sleep(1);
            map.put("foo", "bar");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }


    public void getPriceBy(String priceId) {
        lock.readLock().lock();
        try {
            System.out.println(map.get("foo"));
            sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
    }

}
