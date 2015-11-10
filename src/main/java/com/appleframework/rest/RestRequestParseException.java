/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-5-29
 */
package com.appleframework.rest;

/**
 * <pre>
 *   对请求数据进行解析时发生异常
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@SuppressWarnings("serial")
public class RestRequestParseException extends RestException {
	
    private String requestMessage;

    public String getRequestMessage() {
		return requestMessage;
	}

	public void setRequestMessage(String requestMessage) {
		this.requestMessage = requestMessage;
	}

	public RestRequestParseException(String requestMessage) {
        this(requestMessage, "");
    }

    public RestRequestParseException(String requestMessage, String message) {
        this(requestMessage, message, null);
    }

    public RestRequestParseException(String requestMessage, String message, Throwable cause) {
        super(message, cause);
        this.requestMessage = requestMessage;
    }

    public RestRequestParseException(String requestMessage, Throwable cause) {
        this(requestMessage, null, cause);
    }
}

