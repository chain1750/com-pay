package com.chaincat.pay.exception;

/**
 * 自定义异常
 *
 * @author chenhaizhuang
 */
public class CustomizeException extends RuntimeException {

    public CustomizeException() {
    }

    public CustomizeException(String message) {
        super(message);
    }

    public CustomizeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomizeException(Throwable cause) {
        super(cause);
    }
}
