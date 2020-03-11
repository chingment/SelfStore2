package com.uplink.selfstore.model;

public class ResultBean<T>  {

    private int result;
    private int code;
    private String message;
    private T data;



    public ResultBean(int result,int code ,String message){

        this.result=result;
        this.code=code;
        this.message=message;
    }

    public int getResult() {
        return result;
    }
    public void setResult(int result) {
        this.result = result;
    }

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
