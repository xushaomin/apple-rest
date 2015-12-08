/**
 *
 * 日    期：12-2-8
 */
package com.appleframework.rest.impl;

import com.appleframework.rest.*;
import com.appleframework.rest.config.SystemParameterNames;
import com.appleframework.rest.event.*;
import com.appleframework.rest.marshaller.JacksonJsonRestMarshaller;
import com.appleframework.rest.marshaller.MessageMarshallerUtils;
import com.appleframework.rest.response.ErrorResponse;
import com.appleframework.rest.security.*;
import com.appleframework.rest.security.SecurityManager;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("rawtypes")
public class AnnotationServletServiceRouter implements ServiceRouter {

    public static final String APPLICATION_JSON = "application/json";
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String DEFAULT_EXT_ERROR_BASE_NAME = "i18n/rest/restError";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String I18N_REST_ERROR = "i18n/rest/error";

    private ServiceMethodAdapter serviceMethodAdapter = new AnnotationServiceMethodAdapter();

    private RestMarshaller jsonMarshallerRest = new JacksonJsonRestMarshaller();

    private RequestContextBuilder requestContextBuilder;

    private SecurityManager securityManager;

    private ThreadPoolExecutor threadPoolExecutor;

    private RestContext restContext;

    private RestEventMulticaster restEventMulticaster;

    private List<Interceptor> interceptors = new ArrayList<Interceptor>();

    private List<RestEventListener> listeners = new ArrayList<RestEventListener>();

    private ApplicationContext applicationContext;

    //所有服务方法的最大过期时间，单位为秒(0或负数代表不限制)
    private int serviceTimeoutSeconds = Integer.MAX_VALUE;

    private Class<? extends ThreadFerry> threadFerryClass;

    private String extErrorBasename;

    private String[] extErrorBasenames;

    public void service(Object request, Object response) {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        //获取服务方法最大过期时间
        String method = servletRequest.getParameter(SystemParameterNames.getMethod());
        if (logger.isDebugEnabled()) {
            logger.debug("调用服务方法：" + method);
        }
        int serviceMethodTimeout = getServiceMethodTimeout(method);
        long beginTime = System.currentTimeMillis();

        //使用异常方式调用服务方法
        try {

            //执行线程摆渡
            ThreadFerry threadFerry = buildThreadFerryInstance();
            if (threadFerry != null) {
                threadFerry.doInSrcThread();
            }

            ServiceRunnable runnable = new ServiceRunnable(servletRequest, servletResponse, threadFerry);
            Future<?> future = this.threadPoolExecutor.submit(runnable);
            while (!future.isDone()) {
                future.get(serviceMethodTimeout, TimeUnit.SECONDS);
            }
        } catch (RejectedExecutionException ree) {//超过最大的服务平台的最大资源限制，无法提供服务
            if (logger.isInfoEnabled()) {
                logger.info("调用服务方法:" + method + ")，超过最大资源限制，无法提供服务。");
            }
            RestRequestContext restRequestContext = buildRequestContextWhenException(servletRequest, beginTime);
            MainError invalidMethodError = MainErrors.getError(
                    MainErrorType.SERVICE_CURRENTLY_UNAVAILABLE, restRequestContext.getMethod());
            ErrorResponse restResponse = new ErrorResponse(invalidMethodError);
            writeResponse(restResponse, servletResponse);
            fireAfterDoServiceEvent(restRequestContext);
        } catch (TimeoutException e) {//服务时间超限
            if (logger.isInfoEnabled()) {
                logger.info("调用服务方法:" + method + ")，服务调用超时。");
            }
            RestRequestContext restRequestContext = buildRequestContextWhenException(servletRequest, beginTime);
            
            MainError invalidMethodError = MainErrors.getError(
                    MainErrorType.SERVICE_CURRENTLY_UNAVAILABLE, restRequestContext.getMethod());
            ErrorResponse restResponse = new ErrorResponse(invalidMethodError);
            
            writeResponse(restResponse, servletResponse);
            fireAfterDoServiceEvent(restRequestContext);
        } catch (Throwable throwable) {//产生未知的错误
            if (logger.isInfoEnabled()) {
                logger.info("调用服务方法:" + method + ")，产生异常", throwable);
            }
            RestRequestContext restRequestContext = buildRequestContextWhenException(servletRequest, beginTime);
            
            MainError invalidMethodError = MainErrors.getError(
                    MainErrorType.SERVICE_CURRENTLY_UNAVAILABLE, restRequestContext.getMethod());
            ErrorResponse restResponse = new ErrorResponse(invalidMethodError);
            writeResponse(restResponse, servletResponse);
            fireAfterDoServiceEvent(restRequestContext);
        } finally {
            try {
                servletResponse.getOutputStream().flush();
                servletResponse.getOutputStream().close();
            } catch (IOException e) {
                logger.error("关闭响应出错", e);
            }
        }
    }

    public void startup() {
        if (logger.isInfoEnabled()) {
            logger.info("开始启动Rest框架...");
        }
        Assert.notNull(this.applicationContext, "Spring上下文不能为空");

        //初始化类型转换器
        /*if (this.formattingConversionService == null) {
            this.formattingConversionService = getDefaultConversionService();
        }
        registerConverters(formattingConversionService);*/

        //实例化ServletRequestContextBuilder
        //this.requestContextBuilder = new ServletRequestContextBuilder(this.formattingConversionService);
        this.requestContextBuilder = new ServletRequestContextBuilder();

        //设置校验器
        if (this.securityManager == null) {
            this.securityManager = new DefaultSecurityManager();
        }

        //设置异步执行器
        if (this.threadPoolExecutor == null) {
            this.threadPoolExecutor =
                    new ThreadPoolExecutor(200, Integer.MAX_VALUE, 5 * 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        }

        //创建Rest上下文
        this.restContext = buildRestContext();

        //初始化事件发布器
        this.restEventMulticaster = buildRestEventMulticaster();

        //初始化信息源
        initMessageSource();

        //产生Rest框架初始化事件
        fireAfterStartedRestEvent();

        if (logger.isInfoEnabled()) {
            logger.info("Rest框架启动成功！");
        }
    }

    /*private void registerConverters(FormattingConversionService conversionService) {
        conversionService.addConverter(new RestRequestMessageConverter());
        conversionService.addConverter(new UploadFileConverter());
    }*/

    private ThreadFerry buildThreadFerryInstance() {
        if (threadFerryClass != null) {
            return BeanUtils.instantiate(threadFerryClass);
        } else {
            return null;
        }
    }


    public void shutdown() {
        fireBeforeCloseRestEvent();
        threadPoolExecutor.shutdown();
    }

    public void setThreadFerryClass(Class<? extends ThreadFerry> threadFerryClass) {
        if (logger.isDebugEnabled()) {
            logger.debug("ThreadFerry set to {}",threadFerryClass.getName());
        }
        this.threadFerryClass = threadFerryClass;
    }


    public void setServiceTimeoutSeconds(int serviceTimeoutSeconds) {
        if (logger.isDebugEnabled()) {
            logger.debug("serviceTimeoutSeconds set to {}",serviceTimeoutSeconds);
        }
        this.serviceTimeoutSeconds = serviceTimeoutSeconds;
    }


    public void setSecurityManager(SecurityManager securityManager) {
        if (logger.isDebugEnabled()) {
            logger.debug("securityManager set to {}",securityManager.getClass().getName());
        }
        this.securityManager = securityManager;
    }


    /*public void setFormattingConversionService(FormattingConversionService formatConversionService) {
        if (logger.isDebugEnabled()) {
            logger.debug("formatConversionService set to {}",formatConversionService.getClass().getName());
        }
        this.formattingConversionService = formatConversionService;
    }*/

    /**
     * 获取默认的格式化转换器
     *
     * @return
     */
    /*private FormattingConversionService getDefaultConversionService() {
        FormattingConversionServiceFactoryBean serviceFactoryBean = new FormattingConversionServiceFactoryBean();
        serviceFactoryBean.afterPropertiesSet();
        return serviceFactoryBean.getObject();
    }*/

    public void setExtErrorBasename(String extErrorBasename) {
        if (logger.isDebugEnabled()) {
            logger.debug("extErrorBasename set to {}",extErrorBasename);
        }
        this.extErrorBasename = extErrorBasename;
    }

    public void setExtErrorBasenames(String[] extErrorBasenames) {
        if (extErrorBasenames != null) {
            List<String> list = new ArrayList<String>();
            for (String errorBasename : extErrorBasenames) {
                if (StringUtils.isNotBlank(errorBasename)) {
                    list.add(errorBasename);
                }
            }
            this.extErrorBasenames = list.toArray(new String[0]);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("extErrorBasenames set to {}",extErrorBasenames);
        }
    }


    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
        if (logger.isDebugEnabled()) {
            logger.debug("threadPoolExecutor set to {}",threadPoolExecutor.getClass().getName());
            logger.debug("corePoolSize:{}",threadPoolExecutor.getCorePoolSize());
            logger.debug("maxPoolSize:{}",threadPoolExecutor.getMaximumPoolSize());
            logger.debug("keepAliveSeconds:{} seconds",threadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS));
            logger.debug("queueCapacity:{}",threadPoolExecutor.getQueue().remainingCapacity());
        }
    }


    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    public RestContext getRestContext() {
        return this.restContext;
    }


    public void addInterceptor(Interceptor interceptor) {
        this.interceptors.add(interceptor);
        if (logger.isDebugEnabled()) {
            logger.debug("add  interceptor {}",interceptor.getClass().getName());
        }
    }


    public void addListener(RestEventListener listener) {
        this.listeners.add(listener);
        if (logger.isDebugEnabled()) {
            logger.debug("add  listener {}",listener.getClass().getName());
        }
    }

    public int getServiceTimeoutSeconds() {
        return serviceTimeoutSeconds > 0 ? serviceTimeoutSeconds : Integer.MAX_VALUE;
    }

    /**
     * 取最小的过期时间
     *
     * @param method
     * @param version
     * @return
     */
    private int getServiceMethodTimeout(String method) {
        ServiceMethodHandler serviceMethodHandler = restContext.getServiceMethodHandler(method);
        if (serviceMethodHandler == null) {
            return getServiceTimeoutSeconds();
        } else {
            int methodTimeout = serviceMethodHandler.getServiceMethodDefinition().getTimeout();
            if (methodTimeout <= 0) {
                return getServiceTimeoutSeconds();
            } else {
                return methodTimeout;
            }
        }
    }

    private class ServiceRunnable implements Runnable {

        private HttpServletRequest servletRequest;
        private HttpServletResponse servletResponse;
        private ThreadFerry threadFerry;

        private ServiceRunnable(HttpServletRequest servletRequest,
                                HttpServletResponse servletResponse,
                                ThreadFerry threadFerry) {
            this.servletRequest = servletRequest;
            this.servletResponse = servletResponse;
            this.threadFerry = threadFerry;
        }


        public void run() {
            if (threadFerry != null) {
                threadFerry.doInDestThread();
            }

            RestRequestContext restRequestContext = null;
            RestRequest restRequest = null;
            try {
                //用系统级参数构造一个RequestContext实例（第一阶段绑定）
                restRequestContext = requestContextBuilder.buildBySysParams(
                        restContext, servletRequest, servletResponse);

                //验证系统级参数的合法性
                MainError mainError = securityManager.validateSystemParameters(restRequestContext);
                if (mainError != null) {
                    restRequestContext.setRestResponse(new ErrorResponse(mainError));
                } else {

                    //绑定业务数据（第二阶段绑定）
                    restRequest = requestContextBuilder.buildRestRequest(restRequestContext);

                    //进行其它检查业务数据合法性，业务安全等
                    mainError = securityManager.validateOther(restRequestContext);
                    if (mainError != null) {
                        restRequestContext.setRestResponse(new ErrorResponse(mainError));
                    } else {
                        firePreDoServiceEvent(restRequestContext);

                        //服务处理前拦截
                        invokeBeforceServiceOfInterceptors(restRequestContext);

                        if (restRequestContext.getRestResponse() == null) { //拦截器未生成response
                            //如果拦截器没有产生restResponse时才调用服务方法
                            restRequestContext.setRestResponse(doService(restRequest));

                            //输出响应前拦截
                            invokeBeforceResponseOfInterceptors(restRequest);
                        }
                    }
                }
                //输出响应
                writeResponse(restRequestContext.getRestResponse(), servletResponse);
            } catch (Throwable e) {
                if (restRequestContext != null) {
                	String method = restRequestContext.getMethod();
                	MainError invalidMethodError = null;
                	ErrorResponse restResponse = null;
                	
                    if (logger.isErrorEnabled()) {
                        String message = java.text.MessageFormat.format("service {0} call error", method);
						logger.error(message, e);
                    }
                    
                	if(e.getCause().getClass().equals(UnrecognizedPropertyException.class)
                			|| e.getCause().getClass().equals(JsonMappingException.class)) {
                        invalidMethodError = MainErrors.getError(
                                MainErrorType.INVALID_ARGUMENTS, restRequestContext.getMethod().toString());
                        restResponse = new ErrorResponse(invalidMethodError);
                	}
                	else {
                        invalidMethodError = MainErrors.getError(
                                MainErrorType.SERVICE_CURRENTLY_UNAVAILABLE, restRequestContext.getMethod());
                        restResponse = new ErrorResponse(invalidMethodError);
                	}
                    //输出响应前拦截
                	if(null != restRequest)
                		invokeBeforceResponseOfInterceptors(restRequest);
                    writeResponse(restResponse, servletResponse);

                } else {
                    throw new RestException("RestRequestContext is null.", e);
                }
            } finally {
                if (restRequestContext != null) {

                    //发布服务完成事件
                    restRequestContext.setServiceEndTime(System.currentTimeMillis());

                    fireAfterDoServiceEvent(restRequestContext);
                }
            }
        }
    }


    /**
     * 当发生异常时，创建一个请求上下文对象
     *
     * @param request
     * @param beginTime
     * @return
     */
    private RestRequestContext buildRequestContextWhenException(HttpServletRequest request, long beginTime) {
        RestRequestContext restRequestContext = requestContextBuilder.buildBySysParams(restContext, request, null);
        restRequestContext.setServiceBeginTime(beginTime);
        restRequestContext.setServiceEndTime(System.currentTimeMillis());
        return restRequestContext;
    }

    private RestContext buildRestContext() {
        DefaultRestContext defaultRestContext = new DefaultRestContext(this.applicationContext);
        return defaultRestContext;
    }

    private RestEventMulticaster buildRestEventMulticaster() {

        SimpleRestEventMulticaster simpleRestEventMulticaster = new SimpleRestEventMulticaster();

        //设置异步执行器
        if (this.threadPoolExecutor != null) {
            simpleRestEventMulticaster.setExecutor(this.threadPoolExecutor);
        }

        //添加事件监听器
        if (this.listeners != null && this.listeners.size() > 0) {
            for (RestEventListener restEventListener : this.listeners) {
                simpleRestEventMulticaster.addRestListener(restEventListener);
            }
        }

        return simpleRestEventMulticaster;
    }

    /**
     * 发布Rest启动后事件
     */
    private void fireAfterStartedRestEvent() {
        AfterStartedRestEvent restEvent = new AfterStartedRestEvent(this, this.restContext);
        this.restEventMulticaster.multicastEvent(restEvent);
    }

    /**
     * 发布Rest启动后事件
     */
    private void fireBeforeCloseRestEvent() {
        PreCloseRestEvent restEvent = new PreCloseRestEvent(this, this.restContext);
        this.restEventMulticaster.multicastEvent(restEvent);
    }

    private void fireAfterDoServiceEvent(RestRequestContext restRequestContext) {
        this.restEventMulticaster.multicastEvent(new AfterDoServiceEvent(this, restRequestContext));
    }

    private void firePreDoServiceEvent(RestRequestContext restRequestContext) {
        this.restEventMulticaster.multicastEvent(new PreDoServiceEvent(this, restRequestContext));
    }

    /**
     * 在服务调用之前拦截
     *
     * @param restRequestContext
     */
    private void invokeBeforceServiceOfInterceptors(RestRequestContext restRequestContext) {
        Interceptor tempInterceptor = null;
        try {
            if (interceptors != null && interceptors.size() > 0) {
                for (Interceptor interceptor : interceptors) {

                    interceptor.beforeService(restRequestContext);
                    tempInterceptor = interceptor;
                    //如果有一个产生了响应，则阻止后续的调用
                    if (restRequestContext.getRestResponse() != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("拦截器[" + interceptor.getClass().getName() + "]产生了一个RestResponse," +
                                    " 阻止本次服务请求继续，服务将直接返回。");
                        }
                        return;
                    }
                }
            }
        } catch (Throwable e) {
        	
        	MainError invalidMethodError = MainErrors.getError(
                    MainErrorType.SERVICE_CURRENTLY_UNAVAILABLE, restRequestContext.getMethod());
            ErrorResponse restResponse = new ErrorResponse(invalidMethodError);
                        
            restRequestContext.setRestResponse(restResponse);
            logger.error("在执行拦截器[" + tempInterceptor.getClass().getName() + "]时发生异常.", e);
        }
    }

    /**
     * 在服务调用之后，返回响应之前拦截
     *
     * @param restRequest
     */
    private void invokeBeforceResponseOfInterceptors(RestRequest restRequest) {
        RestRequestContext restRequestContext = restRequest.getRestRequestContext();
        Interceptor tempInterceptor = null;
        try {
            if (interceptors != null && interceptors.size() > 0) {
                for (Interceptor interceptor : interceptors) {
                    interceptor.beforeResponse(restRequestContext);
                    tempInterceptor = interceptor;
                }
            }
        } catch (Throwable e) {
        	MainError invalidMethodError = MainErrors.getError(
                    MainErrorType.SERVICE_CURRENTLY_UNAVAILABLE, restRequestContext.getMethod());
            ErrorResponse restResponse = new ErrorResponse(invalidMethodError);
            
            restRequestContext.setRestResponse(restResponse);
            logger.error("在执行拦截器[" + tempInterceptor.getClass().getName() + "]时发生异常.", e);
        }
    }

    private void writeResponse(Object restResponse, HttpServletResponse httpServletResponse) {
        try {
        	MessageFormat messageFormat = MessageFormat.json;
            if (!(restResponse instanceof ErrorResponse) && messageFormat == MessageFormat.stream) {
                if (logger.isDebugEnabled()) {
                    logger.debug("使用{}输出方式，由服务自身负责响应输出工作.", MessageFormat.stream);
                }
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("输出响应：" + MessageMarshallerUtils.getMessage(restResponse, messageFormat));
            }
            RestMarshaller restMarshaller = jsonMarshallerRest;
            String contentType = APPLICATION_JSON;
            if (messageFormat == MessageFormat.json) {
                restMarshaller = jsonMarshallerRest;
                contentType = APPLICATION_JSON;
            }
            httpServletResponse.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            httpServletResponse.addHeader(ACCESS_CONTROL_ALLOW_METHODS, "*");
            httpServletResponse.setCharacterEncoding(Constants.UTF8);
            httpServletResponse.setContentType(contentType);
            restMarshaller.marshaller(restResponse, httpServletResponse.getOutputStream());
        } catch (IOException e) {
            throw new RestException(e);
        }
    }

    private Object doService(RestRequest restRequest) {
        Object restResponse = null;
        RestRequestContext context = restRequest.getRestRequestContext();
        if (context.getMethod() == null) {
            restResponse = new ErrorResponse(MainErrors.getError(MainErrorType.MISSING_METHOD,
                    SystemParameterNames.getMethod()));
        } else if (!restContext.isValidMethod(context.getMethod())) {
            MainError invalidMethodError = MainErrors.getError(
                    MainErrorType.INVALID_METHOD,context.getMethod());
            restResponse = new ErrorResponse(invalidMethodError);
        } else {
            try {
                restResponse = serviceMethodAdapter.invokeServiceMethod(restRequest);
            } catch (Exception e) { //出错则招聘服务不可用的异常
                if (logger.isInfoEnabled()) {
                    logger.info("调用" + context.getMethod() + "时发生异常，异常信息为：" + e.getMessage());
                    e.printStackTrace();
                }
                MainError invalidMethodError = MainErrors.getError(
                        MainErrorType.BUSINESS_LOGIC_ERROR,context.getMethod());
                restResponse = new ErrorResponse(invalidMethodError);
            }
        }
        return restResponse;
    }

    /**
     * 设置国际化资源信息
     */
    private void initMessageSource() {
        HashSet<String> baseNamesSet = new HashSet<String>();
        baseNamesSet.add(I18N_REST_ERROR);//REST自动的资源

        if (extErrorBasename == null && extErrorBasenames == null) {
            baseNamesSet.add(DEFAULT_EXT_ERROR_BASE_NAME);
        } else {
            if (extErrorBasename != null) {
                baseNamesSet.add(extErrorBasename);
            }
            if (extErrorBasenames != null) {
                baseNamesSet.addAll(Arrays.asList(extErrorBasenames));
            }
        }
        String[] totalBaseNames = baseNamesSet.toArray(new String[0]);

        if (logger.isInfoEnabled()) {
            logger.info("加载错误码国际化资源：{}", StringUtils.join(totalBaseNames, ","));
        }
        ResourceBundleMessageSource bundleMessageSource = new ResourceBundleMessageSource();
        bundleMessageSource.setBasenames(totalBaseNames);
        MessageSourceAccessor messageSourceAccessor = new MessageSourceAccessor(bundleMessageSource);
        MainErrors.setErrorMessageSourceAccessor(messageSourceAccessor);
        SubErrors.setErrorMessageSourceAccessor(messageSourceAccessor);
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }


    /*public FormattingConversionService getFormattingConversionService() {
        return formattingConversionService;
    }*/

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    public RestEventMulticaster getRestEventMulticaster() {
        return restEventMulticaster;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public List<RestEventListener> getListeners() {
        return listeners;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public String getExtErrorBasename() {
        return extErrorBasename;
    }

}