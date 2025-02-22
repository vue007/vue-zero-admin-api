package com.zero.admin.base.core.domain;


import com.zero.admin.base.core.constant.HttpStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class Rt<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final int SUCCESS = 200;
    public static final int FAIL = 500;

    private int code;
    private String msg;
    private T data;


    public static <T> Rt<T> ok() {
        return restResult(null, SUCCESS, "操作成功");
    }

    public static <T> Rt<T> ok(T data) {
        return restResult(data, SUCCESS, "操作成功");
    }

    public static <T> Rt<T> ok(String msg) {
        return restResult(null, SUCCESS, msg);
    }

    public static <T> Rt<T> ok(String msg, T data) {
        return restResult(data, SUCCESS, msg);
    }

    public static <T> Rt<T> fail() {
        return restResult(null, FAIL, "操作失败");
    }

    public static <T> Rt<T> fail(String msg) {
        return restResult(null, FAIL, msg);
    }

    public static <T> Rt<T> fail(T data) {
        return restResult(data, FAIL, "操作失败");
    }

    public static <T> Rt<T> fail(String msg, T data) {
        return restResult(data, FAIL, msg);
    }

    public static <T> Rt<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }

    /**
     * 返回警告消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static <T> Rt<T> warn(String msg) {
        return restResult(null, HttpStatus.WARN, msg);
    }

    /**
     * 返回警告消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static <T> Rt<T> warn(String msg, T data) {
        return restResult(data, HttpStatus.WARN, msg);
    }

    private static <T> Rt<T> restResult(T data, int code, String msg) {
        Rt<T> r = new Rt<>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }

    public static <T> Boolean isError(Rt<T> ret) {
        return !isSuccess(ret);
    }

    public static <T> Boolean isSuccess(Rt<T> ret) {
        return Rt.SUCCESS == ret.getCode();
    }
}
