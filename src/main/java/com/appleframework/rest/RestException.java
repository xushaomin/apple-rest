/**
 *
 * 日    期：12-2-13
 */
package com.appleframework.rest;

/**
 * <pre>
 *   REST的异常。
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@SuppressWarnings("serial")
public class RestException extends RuntimeException {
	
    public RestException() {
    }

    public RestException(String message) {
        super(message);
    }

    public RestException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestException(Throwable cause) {
        super(cause);
    }
}

