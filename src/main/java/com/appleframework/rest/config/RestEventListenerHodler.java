/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-5
 */
package com.appleframework.rest.config;

import com.appleframework.rest.event.RestEventListener;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class RestEventListenerHodler {

	private RestEventListener restEventListener;

    public RestEventListenerHodler(RestEventListener restEventListener) {
        this.restEventListener = restEventListener;
    }

    public RestEventListener getRestEventListener() {
        return restEventListener;
    }
}

