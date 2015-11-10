/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-1
 */
package com.appleframework.rest.event;

import com.appleframework.rest.RestContext;

/**
 * <pre>
 *   在Rest框架初始化后产生的事件
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@SuppressWarnings("serial")
public class AfterStartedRestEvent extends RestEvent {

    public AfterStartedRestEvent(Object source, RestContext restContext) {
        super(source, restContext);
    }

}

