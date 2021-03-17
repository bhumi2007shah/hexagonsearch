/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Cache implementation that times out entries after a specific time duration
 *
 * @author : Shital Raval
 * Date : 6/3/20
 * Time : 12:35 PM
 * Class Name : TimedCache
 * Project Name : server
 */
public class TimedCache {
    private final ConcurrentHashMap<String, Object> objectMap = new ConcurrentHashMap<String, Object>(10);
    private final ConcurrentHashMap<String, Long> timeMap = new ConcurrentHashMap<String, Long>();
    private final ReentrantReadWriteLock accessLock = new ReentrantReadWriteLock();
    private final Runnable evictor = new Runnable() {

        /**
         * thread that removes expired data
         */
        @Override
        public void run() {
            // do not run on empty maps
            if(timeMap.isEmpty()){
                Thread.yield();
            }
            long currentTime = System.nanoTime();
            accessLock.writeLock().lock();
            Set<String> keys = new HashSet<String>(timeMap.keySet());
            accessLock.writeLock().unlock();


            Set<String> markedForRemoval = new HashSet<String>(10);
            for (String key : keys) {
                long lastTime = timeMap.get(key);
                if(lastTime == 0){
                    continue;
                }
                long interval = currentTime - lastTime;
                long elapsedTime = TimeUnit.MINUTES.convert(interval, expiryTimeUnit);
                if(elapsedTime >= expiryTime){
                    markedForRemoval.add(key);
                }
            }

            accessLock.writeLock().lock();
            for (String key : markedForRemoval) {
                long lastTime = timeMap.get(key);
                if(lastTime == 0){
                    continue;
                }
                long interval = currentTime - lastTime;
                long elapsedTime = TimeUnit.MINUTES.convert(interval, expiryTimeUnit);
                if(elapsedTime >= expiryTime){
                    remove(key);
                }
            }
            accessLock.writeLock().unlock();
        }
    };

    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory(true));
    private final class MyThreadFactory implements ThreadFactory {

        private boolean isDaemon = false;

        public MyThreadFactory(boolean daemon){
            isDaemon = daemon;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(isDaemon);
            return t;
        }

    };
    private final long expiryTime;
    private final TimeUnit expiryTimeUnit;

    public TimedCache(long initialDelay, long evictionDelay, long expiryTime, TimeUnit unit){
        timer.scheduleWithFixedDelay(evictor, initialDelay, evictionDelay, unit);
        this.expiryTime = expiryTime;
        this.expiryTimeUnit = unit;
    }

    public void put(String key, Object value) {
        accessLock.readLock().lock();
        Long nanoTime = System.nanoTime();
        timeMap.put(key, nanoTime);
        objectMap.put(key, value);
        accessLock.readLock().unlock();
    }

    public Object get(String key) {
        accessLock.readLock().lock();
        Object value = objectMap.get(key);
        accessLock.readLock().unlock();
        return value;
    }

    public Object remove(Object key) {
        accessLock.readLock().lock();
        //accessLock.lock();
        Object value = objectMap.remove(key);
        timeMap.remove(key);
        //accessLock.unlock();
        accessLock.readLock().unlock();
        return value;

    }
}
