package com.simple.commons.model;

/**
 * RPC Response
 * @author huangyong
 * @author xu.qiang
 * @date 17/8/9
 */
public class RpcResponse {

    //与每个请求对应
    private int opaque;
    private String error;
    private Object result;

    public boolean isError() {
        return error != null;
    }

    public int getOpaque() {
        return opaque;
    }

    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
