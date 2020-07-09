package com.client.exception;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2020年3月25日 下午5:00:42
 */
public class LockTimeoutException extends RuntimeException {

    private static final long serialVersionUID = 6142567438199986700L;

    public LockTimeoutException() {
    }

    public LockTimeoutException(String message) {
        super(message);
    }

    public LockTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockTimeoutException(Throwable cause) {
        super(cause);
    }

    public LockTimeoutException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
