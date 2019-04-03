package com.cx.exception;

/**
 * 乐观锁异常
 *
 * Created by Alan
 */
public class RedisOptimisticLockException extends RuntimeException{

    private final static String MESSAGE = "None command execute results return, call by this is redis OptimisticLock ,just see watch(key) command";

    public RedisOptimisticLockException() {
        super(MESSAGE);
    }
}
