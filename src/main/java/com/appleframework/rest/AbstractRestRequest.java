/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-6
 */
package com.appleframework.rest;

import com.appleframework.rest.annotation.Temporary;

/**
 * <pre>
 *   所有请求对象应该通过扩展此抽象类实现
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public abstract class AbstractRestRequest implements RestRequest {

    @Temporary
    private RestRequestContext restRequestContext;

    @Override
    public RestRequestContext getRestRequestContext() {
        return restRequestContext;
    }

    public final void setRestRequestContext(RestRequestContext restRequestContext) {
        this.restRequestContext = restRequestContext;
    }
}

