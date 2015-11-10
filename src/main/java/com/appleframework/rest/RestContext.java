/**
 *
 * 日    期：12-2-11
 */
package com.appleframework.rest;

import java.util.Map;

/**
 * <pre>
 *    REST服务方法的处理者的注册表
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public interface RestContext {

    /**
     * 注册一个服务处理器
     *
     * @param methodName
     * @param version
     * @param serviceMethodHandler
     */
    void addServiceMethod(String methodName, ServiceMethodHandler serviceMethodHandler);

    /**
     * 获取服务处理器
     *
     * @param methodName
     * @return
     */
    ServiceMethodHandler getServiceMethodHandler(String methodName);

    /**
     * 是否是合法的服务方法
     *
     * @param methodName
     * @return
     */
    boolean isValidMethod(String methodName);


    /**
     * 获取所有的处理器列表
     *
     * @return
     */
    Map<String, ServiceMethodHandler> getAllServiceMethodHandlers();

}

