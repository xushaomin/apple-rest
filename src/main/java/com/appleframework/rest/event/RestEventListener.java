/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-2
 */
package com.appleframework.rest.event;

import java.util.EventListener;

/**
 * <pre>
 *    监听所有Rest框架的事件
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public interface RestEventListener<E extends RestEvent> extends EventListener {

    /**
     * 响应事件
     *
     * @param restEvent
     */
    void onRestEvent(E restEvent);

    /**
     * 执行的顺序号
     *
     * @return
     */
    int getOrder();
}

