package com.solovev.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseResult<T> {
    private String message;
    private T data;

    /**
     * For serialization purposes
     */
    public ResponseResult() {
    }

    public ResponseResult(T data) {
        this.data = data;
    }

    public ResponseResult(String message) {
        this.message = message;
    }

    public ResponseResult(String message, T data) {
        this.message = message;
        this.data = data;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseResult<?> that = (ResponseResult<?>) o;
        return Objects.equals(message, that.message) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, data);
    }

    @Override
    public String toString() {
        return "ResponseResult{" +
                "message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
