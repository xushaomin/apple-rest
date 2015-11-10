/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-2
 */
package com.appleframework.rest.event;

import com.appleframework.rest.RestRequestContext;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@SuppressWarnings("serial")
public class AfterDoServiceEvent extends RestEvent {

    private RestRequestContext restRequestContext;

    public AfterDoServiceEvent(Object source, RestRequestContext restRequestContext) {
        super(source, restRequestContext.getRestContext());
        this.restRequestContext = restRequestContext;
    }

    public long getServiceBeginTime() {
        return restRequestContext.getServiceBeginTime();
    }

    public long getServiceEndTime() {
        return restRequestContext.getServiceEndTime();
    }

    public RestRequestContext getRestRequestContext() {
        return restRequestContext;
    }
}

