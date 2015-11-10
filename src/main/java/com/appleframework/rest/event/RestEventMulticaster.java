/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-2
 */
package com.appleframework.rest.event;

/**
 * <pre>
 *   注册事件监听器，发布事件
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public interface RestEventMulticaster {

    /**
     * Add a listener to be notified of all events.
     *
     * @param listener the listener to add
     */
	void addRestListener(RestEventListener listener);

    /**
     * Remove a listener from the notification list.
     *
     * @param listener the listener to remove
     */
    void removeRestListener(RestEventListener listener);

    /**
     * Remove all listeners registered with this multicaster.
     * <p>After a remove call, the multicaster will perform no action
     * on event notification until new listeners are being registered.
     */
    void removeAllRestListeners();

    /**
     * Multicast the given application event to apprestriate listeners.
     *
     * @param event the event to multicast
     */
    void multicastEvent(RestEvent event);
}

