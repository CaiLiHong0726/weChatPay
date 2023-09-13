package com.hong.wechatpay.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DefaultException extends Exception {

    public DefaultException() {
        super();
    }

    public DefaultException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DefaultException(String message, Throwable cause) {
        super(message, cause);
    }

    public DefaultException(String message) {
        super(message);
    }

    public DefaultException(Throwable cause) {
        super(cause);
    }


}
