/**
 *
 * 日    期：12-2-13
 */
package com.appleframework.rest;

import com.appleframework.rest.event.RestEventListener;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * <pre>
 *      REST的服务路由器，服务方法必须位于@Controller的类中，服务方法使用{@link com.appleframework.rest.annotation.ServiceMethod}注解，有两个合法的方法签名方式：
 * 签名方式1：有入参，且参数必须实现{@link RestRequest}接口，返回参数为{@link RestResponse}
 *   <code>
 *    @ServiceMethod("method1")
 *    RestResponse handleMethod1(RestRequest restestRequest){
 *        ...
 *    }
 *   </code>
 * 签名方式2：无入参，返回参数为{@link RestResponse}
 *   <code>
 *    @ServiceMethod("method1")
 *    RestResponse handleMethod1(){
 *        ...
 *    }
 *   </code>
 *   REST框架会自动将请求参数的值绑定到入参请求对象中。
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public interface ServiceRouter {

    /**
     * REST框架的总入口，一般框架实现，开发者无需关注。
     *
     * @param request
     * @param response
     */
    void service(Object request, Object response);

    /**
     * 启动服务路由器
     */
    void startup();

    /**
     * 关闭服务路由器
     */
    void shutdown();

    /**
     * 获取{@link RestContext}
     *
     * @return
     */
    RestContext getRestContext();

    /**
     * 设置Spring的上下文
     *
     * @param applicationContext
     */
    void setApplicationContext(ApplicationContext applicationContext);

    /**
     * 注册拦截器
     *
     * @param interceptor
     */
    void addInterceptor(Interceptor interceptor);

    /**
     * 注册监听器
     *
     * @param listener
     */
    @SuppressWarnings("rawtypes")
	void addListener(RestEventListener listener);

    /**
     * 设置{@link com.appleframework.rest.security.SecurityManager}
     *
     * @param securityManager
     */
    void setSecurityManager(com.appleframework.rest.security.SecurityManager securityManager);

    /**
     * 注册
     *
     * @param threadPoolExecutor
     */
    void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor);

    /**
     * 设置所有服务的通用过期时间，单位为秒
     *
     * @param serviceTimeoutSeconds
     */
    void setServiceTimeoutSeconds(int serviceTimeoutSeconds);

    /**
     * 设置扩展错误资源基名
     *
     * @param extErrorBasename
     */
    void setExtErrorBasename(String extErrorBasename);

    /**
     * 允许设置多个资源文件
     * @param extErrorBasenames
     */
    void setExtErrorBasenames(String[] extErrorBasenames);

    /**
     * 设置线程信息摆渡器
     * @param threadFerryClass
     */
    void setThreadFerryClass(Class<? extends ThreadFerry> threadFerryClass);
}

