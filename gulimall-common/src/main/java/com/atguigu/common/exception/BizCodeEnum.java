package com.atguigu.common.exception;

public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "Unknown error in the system"),
    VALID_EXCEPTION(10001, "Parameter Authentication Failure");

    private int code;
    private String message;

    BizCodeEnum(int code, String message) {
        this.code=code;
        this.message=message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
