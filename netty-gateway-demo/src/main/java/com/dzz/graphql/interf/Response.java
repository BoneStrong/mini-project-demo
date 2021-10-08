package com.dzz.graphql.interf;

import java.beans.Transient;

public class Response<T> {

    private static final String SUCCESS_CODE = "0";

    private String code;

    private String message;

    private T data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
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

    public static <T> Response<T> of(T data) {
        Response<T> response = new Response<>();
        response.setData(data);
        return response;
    }

    public static <T> Response<PageableData<T>> ofPageableData(PageableData<T> data) {
        Response<PageableData<T>> response = new Response<>();
        response.setData(data);
        return response;
    }

    public Response<T> withCode(String code) {
        this.code = code;
        return this;
    }

    public Response<T> withCode(int code) {
        this.code = Integer.toString(code);
        return this;
    }

    public Response<T> withMessage(String message) {
        this.message = message;
        return this;
    }

    public static <T> Response<T> success() {
        return new Response<T>().withCode(SUCCESS_CODE).withMessage("success");
    }

    public static <T> Response<T> success(T data) {
        return Response.of(data).withCode(SUCCESS_CODE).withMessage("success");
    }

    public static <T> Response<PageableData<T>> success(PageableData<T> pageableData) {
        return Response.ofPageableData(pageableData).withCode(SUCCESS_CODE).withMessage("success");
    }

    public static <T> Response<T> fail(String code, String message) {
        return new Response<T>().withCode(code).withMessage(message);
    }

    public static <T> Response<T> fail(int code, String message) {
        return new Response<T>().withCode(Integer.toString(code)).withMessage(message);
    }

    @Transient
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(this.getCode());
    }

}
