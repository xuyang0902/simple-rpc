package com.simple.rpc.exception;

/**
 * 框架对外异常包装
 * @author xu.qiang
 * @date 17/8/10
 */
public class RpcException extends RuntimeException{


    private static final long serialVersionUID = -7134751566184184588L;

    public RpcException(){
        super();
    }

    public RpcException(String message){
        super(message);
    }

    public RpcException(Throwable thr){
        super(thr);
    }

}
