package com.sky.exception;

/**
 * 用户Id非法异常
 */
public class WrongIdException extends BaseException{
    public WrongIdException(){}

    public WrongIdException(String msg) {
        super(msg);
    }
}
