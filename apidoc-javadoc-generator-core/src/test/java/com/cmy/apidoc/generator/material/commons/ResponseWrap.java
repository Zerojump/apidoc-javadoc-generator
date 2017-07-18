package com.cmy.apidoc.generator.material.commons;

import com.cmy.apidoc.generator.annotations.ApiErrorFactoryMethod;

import java.io.Serializable;

/**
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>date: 2017/7/12
 */
public class ResponseWrap<T> implements Serializable {
    /**
     * 状态码
     */
    private int code;

    /**
     * 描述信息
     */
    private String desc;
    /**
     * 数据域
     */
    private T data;

    @ApiErrorFactoryMethod(value = "parameterError", statusCode = 401, desc = "please check your parameter is right")
    public static ResponseWrap getParamError() {
        ResponseWrap<?> responseWrap = new ResponseWrap<>();
        responseWrap.setCode(303);
        return responseWrap;
    }

    public static ResponseWrap getPasswordError() {
        ResponseWrap<?> responseWrap = new ResponseWrap<>();
        responseWrap.setCode(301);
        return responseWrap;
    }

    public static ResponseWrap getAccountError() {
        ResponseWrap<?> responseWrap = new ResponseWrap<>();
        responseWrap.setCode(302);
        return responseWrap;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return code == 100;
    }

    @Override
    public String toString() {
        return "ResponseWrap{" +
                "code=" + code +
                ", desc='" + desc + '\'' +
                ", data=" + data +
                '}';
    }
}
