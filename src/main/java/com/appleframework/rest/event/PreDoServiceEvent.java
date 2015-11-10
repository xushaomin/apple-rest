/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-1
 */
package com.appleframework.rest.event;

import com.appleframework.rest.RestRequestContext;

/**
 * <pre>
 *    在执行服务方法之前产生的事件
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@SuppressWarnings("serial")
public class PreDoServiceEvent extends RestEvent {

    private RestRequestContext restRequestContext;

    public PreDoServiceEvent(Object source, RestRequestContext restRequestContext) {
        super(source, restRequestContext.getRestContext());
        this.restRequestContext = restRequestContext;
    }

    public RestRequestContext getRestRequestContext() {
        return restRequestContext;
    }

    public long getServiceBeginTime() {
        return restRequestContext.getServiceBeginTime();
    }
}

