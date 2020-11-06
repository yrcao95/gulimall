package com.atguigu.common.constant;

public class WareConstant {
    public enum PurchaseStatusEnum {
        CREATED(0, "Created"),
        ASSIGNED(1, "Assigned"),
        RECEIVED(2, "Received"),
        FINISHED(3, "Finished"),
        ERROR(4, "Error");

        private int code;
        private String msg;

        PurchaseStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum PurchaseDetailStatusEnum {
        CREATED(0, "Created"),
        ASSIGNED(1, "Assigned"),
        BUYING(2, "Buying"),
        FINISHED(3, "Finished"),
        FAILED(4, "Purchase Failed");

        private int code;
        private String msg;

        PurchaseDetailStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
