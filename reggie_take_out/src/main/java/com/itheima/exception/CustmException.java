package com.itheima.exception;

/**
 * 自定义业务异常类
 */
public class CustmException extends RuntimeException{
    public CustmException(String msg){
        super(msg);
    }
}
