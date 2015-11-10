/**
 *
 * 日    期：12-2-27
 */
package com.appleframework.rest;

import com.appleframework.rest.annotation.HttpAction;

import java.util.Map;

/**
 * <pre>
 *    接到服务请求后，将产生一个{@link RestRequestContext}上下文对象，它被本次请求直到返回响应的这个线程共享。
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public interface RestRequestContext {

    /**
     * 获取Rest的上下文
     *
     * @return
     */
    RestContext getRestContext();

    /**
     * 获取服务的方法
     *
     * @return
     */
    String getMethod();

    /**
     * 获取内容
     *
     * @return
     */
    String getContent();


    /**
     * 获取客户端的IP
     *
     * @return
     */
    String getIp();

    /**
     * 获取请求的方法 如POST
     */
    HttpAction getHttpAction();

    /**
     * 获取请求的原对象（如HttpServletRequest）
     *
     * @return
     */
    Object getRawRequestObject();

    /**
     * 获取请求的原响应对象（如HttpServletResponse）
     * @return
     */
    Object getRawResponseObject();

    /**
     * 设置服务开始时间
     *
     * @param serviceBeginTime
     */
    void setServiceBeginTime(long serviceBeginTime);

    /**
     * 获取服务开始时间，单位为毫秒，为-1表示无意义
     *
     * @return
     */
    long getServiceBeginTime();

    /**
     * 设置服务开始时间
     *
     * @param serviceEndTime
     */
    void setServiceEndTime(long serviceEndTime);

    /**
     * 获取服务结束时间，单位为毫秒，为-1表示无意义
     *
     * @return
     */
    long getServiceEndTime();

    /**
     * 获取服务方法对应的ApiMethod对象信息
     *
     * @return
     */
    ServiceMethodDefinition getServiceMethodDefinition();

    /**
     * 获取服务的处理者
     *
     * @return
     */
    ServiceMethodHandler getServiceMethodHandler();


    /**
     * @param restResponse
     */
    void setRestResponse(Object restResponse);

    /**
     * 返回响应对象
     *
     * @return
     */
    Object getRestResponse();


    /**
     * 获取特定属性
     *
     * @param name
     * @return
     */
    Object getAttribute(String name);

    /**
     * 设置属性的值
     *
     * @param name
     * @param value
     */
    void setAttribute(String name, Object value);

    /**
     * 获取请求参数列表
     *
     * @return
     */
    Map<String, String> getAllParams();

    /**
     * 获取请求参数值
     *
     * @param paramName
     * @return
     */
    String getParamValue(String paramName);

    /**
     * 获取请求ID，是一个唯一的UUID，每次请求对应一个唯一的ID
     * @return
     */
    String getRequestId();
}

