/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-2
 */
package com.appleframework.rest.event;

import com.appleframework.rest.RestContext;

import java.util.EventObject;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@SuppressWarnings("serial")
public abstract class RestEvent extends EventObject {

    private RestContext restContext;

    public RestEvent(Object source, RestContext restContext) {
        super(source);
        this.restContext = restContext;
    }

    public RestContext getRestContext() {
        return restContext;
    }
}

