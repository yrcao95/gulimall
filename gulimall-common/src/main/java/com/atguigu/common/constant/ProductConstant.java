package com.atguigu.common.constant;

public class ProductConstant {

    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "Fundamental Properties"), ATTR_TYPE_SALE(0, "Sales Properties");

        private int code;
        private String msg;

        AttrEnum(int code, String msg) {
            this.code=code;
            this.msg=msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum StatusEnum {
        NEW_SPU(0, "New spu"),
        SPU_UP(1, "spu up for sale"),
        SPU_DOWN(2, "spu off");

        private int code;
        private String msg;

        StatusEnum(int code, String msg) {
            this.code=code;
            this.msg=msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
