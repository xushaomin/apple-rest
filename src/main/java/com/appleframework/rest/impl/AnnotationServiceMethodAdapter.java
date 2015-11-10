/**
 * 日    期：12-2-11
 */
package com.appleframework.rest.impl;

import com.appleframework.rest.RestRequest;
import com.appleframework.rest.RestRequestContext;
import com.appleframework.rest.ServiceMethodAdapter;
import com.appleframework.rest.ServiceMethodHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;


/**
 * <pre>
 *    通过该服务方法适配器调用目标的服务方法
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class AnnotationServiceMethodAdapter implements ServiceMethodAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * 调用REST服务方法
     *
     * @param restRequest
     * @return
     */
    public Object invokeServiceMethod(RestRequest restRequest) {
        try {
            RestRequestContext restRequestContext = restRequest.getRestRequestContext();
            //分析上下文中的错误
            ServiceMethodHandler serviceMethodHandler = restRequestContext.getServiceMethodHandler();
            if (logger.isDebugEnabled()) {
                logger.debug("执行" + serviceMethodHandler.getHandler().getClass() +
                        "." + serviceMethodHandler.getHandlerMethod().getName());
            }
            if (serviceMethodHandler.isHandlerMethodWithParameter()) {
                return serviceMethodHandler.getHandlerMethod().invoke(
                        serviceMethodHandler.getHandler(),restRequest);
            } else {
                return serviceMethodHandler.getHandlerMethod().invoke(serviceMethodHandler.getHandler());
            }
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                InvocationTargetException inve = (InvocationTargetException) e;
                throw new RuntimeException(inve.getTargetException());
            } else {
                throw new RuntimeException(e);
            }
        }
    }

}

