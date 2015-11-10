/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-30
 */
package com.appleframework.rest.client;

import com.appleframework.rest.response.ErrorResponse;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class DefaultCompositeResponse<T> implements CompositeResponse {

    private boolean successful;

    private ErrorResponse errorResponse;

    private T successRestResponse;

    public DefaultCompositeResponse(boolean successful) {
        this.successful = successful;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return this.errorResponse;
    }

    @Override
    public T getSuccessResponse() {
        return this.successRestResponse;
    }

    public void setErrorResponse(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public void setSuccessRestResponse(T successRestResponse) {
        this.successRestResponse = successRestResponse;
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }
}

