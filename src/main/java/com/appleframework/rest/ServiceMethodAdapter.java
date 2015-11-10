/**
 *
 * 日    期：12-2-11
 */
package com.appleframework.rest;

/**
 * <pre>
 *    通过该适配器以统一的方式调用REST方法
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public interface ServiceMethodAdapter {
    /**
     * 调用服务方法
     *
     * @param restRequest
     * @return
     */
    Object invokeServiceMethod(RestRequest restRequest);

}

