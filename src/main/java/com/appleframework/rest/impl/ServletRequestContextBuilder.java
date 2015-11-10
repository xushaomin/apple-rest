/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-1
 */
package com.appleframework.rest.impl;

import com.appleframework.rest.*;
import com.appleframework.rest.annotation.HttpAction;
import com.appleframework.rest.client.RestUnmarshaller;
import com.appleframework.rest.client.unmarshaller.JacksonJsonRestUnmarshaller;
import com.appleframework.rest.config.SystemParameterNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *    构建{@link com.appleframework.rest.RestRequestContext}实例
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class ServletRequestContextBuilder implements RequestContextBuilder {

    //通过前端的负载均衡服务器时，请求对象中的IP会变成负载均衡服务器的IP，因此需要特殊处理，下同。
    public static final String X_REAL_IP = "X-Real-IP";

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private FormattingConversionService conversionService;

    private Validator validator;
    
    private RestUnmarshaller jsonUnmarshaller = new JacksonJsonRestUnmarshaller();

    public ServletRequestContextBuilder(FormattingConversionService conversionService) {
        this.conversionService = conversionService;
    }
    
    public ServletRequestContextBuilder() {
    }

    @Override
    public SimpleRestRequestContext buildBySysParams(RestContext restContext,
                                                    Object request,
                                                    Object response) {
        if (!(request instanceof HttpServletRequest)) {
            throw new IllegalArgumentException("请求对象必须是HttpServletRequest的类型");
        }
        if(response != null && !(response instanceof HttpServletResponse)){
            throw new IllegalArgumentException("请求对象必须是HttpServletResponse的类型");
        }

        HttpServletRequest servletRequest = (HttpServletRequest) request;
        SimpleRestRequestContext requestContext = new SimpleRestRequestContext(restContext);

        //设置请求对象及参数列表
        requestContext.setRawRequestObject(servletRequest);
        if (response != null) {
            requestContext.setRawResponseObject(response);
        }
        requestContext.setAllParams(getRequestParams(servletRequest));
        requestContext.setIp(getRemoteAddr(servletRequest)); //感谢melin所指出的BUG

        //设置服务的系统级参数
        requestContext.setMethod(servletRequest.getParameter(SystemParameterNames.getMethod()));
        requestContext.setContent(servletRequest.getParameter(SystemParameterNames.getContent()));
        requestContext.setHttpAction(HttpAction.fromValue(servletRequest.getMethod()));

        //设置服务处理器
        ServiceMethodHandler serviceMethodHandler =
                restContext.getServiceMethodHandler(requestContext.getMethod());
        requestContext.setServiceMethodHandler(serviceMethodHandler);

        return requestContext;
    }

    private String getRemoteAddr(HttpServletRequest request) {
        String remoteIp = request.getHeader(X_REAL_IP); //nginx反向代理
        if (StringUtils.hasText(remoteIp)) {
            return remoteIp;
        } else {
            remoteIp = request.getHeader(X_FORWARDED_FOR);//apache反射代理
            if (StringUtils.hasText(remoteIp)) {
                String[] ips = remoteIp.split(",");
                for (String ip : ips) {
                    if (!"null".equalsIgnoreCase(ip)) {
                        return ip;
                    }
                }
            }
            return request.getRemoteAddr();
        }
    }

    /**
     * 将{@link HttpServletRequest}的数据绑定到{@link com.appleframework.rest.RestRequestContext}的{@link com.appleframework.rest.RestRequest}中，同时使用
     * JSR 303对请求数据进行校验，将错误信息设置到{@link com.appleframework.rest.RestRequestContext}的属性列表中。
     *
     * @param restRequestContext
     */
    @Override
    public RestRequest buildRestRequest(RestRequestContext restRequestContext) {
        AbstractRestRequest restRequest = null;
        if (restRequestContext.getServiceMethodHandler().isRestRequestImplType()) {
            HttpServletRequest request =
                    (HttpServletRequest) restRequestContext.getRawRequestObject();
            
            //修改 对传json字符串的支持
            String content = restRequestContext.getContent();
            BindingResult bindingResult = null;
            if(null != content) {
                bindingResult = doBind(request, content, restRequestContext.getServiceMethodHandler().getRequestType());
            }
            else {
                bindingResult = doBind(request, restRequestContext.getServiceMethodHandler().getRequestType());
            }
            restRequest = buildRestRequestFromBindingResult(restRequestContext, bindingResult);

            List<ObjectError> allErrors = bindingResult.getAllErrors();
            restRequestContext.setAttribute(SimpleRestRequestContext.SPRING_VALIDATE_ERROR_ATTRNAME, allErrors);
        } else {
            restRequest = new DefaultRestRequest();
        }
        restRequest.setRestRequestContext(restRequestContext);
        return restRequest;
    }

    private AbstractRestRequest buildRestRequestFromBindingResult(RestRequestContext restRequestContext, BindingResult bindingResult) {
        AbstractRestRequest restRequest = (AbstractRestRequest) bindingResult.getTarget();
        if (restRequest instanceof AbstractRestRequest) {
            AbstractRestRequest abstractRestRequest = restRequest;
            abstractRestRequest.setRestRequestContext(restRequestContext);
        } else {
            logger.warn(restRequest.getClass().getName() + "不是扩展于" + AbstractRestRequest.class.getName() +
                    ",无法设置" + RestRequestContext.class.getName());
        }
        return restRequest;
    }

    @SuppressWarnings("rawtypes")
	private HashMap<String, String> getRequestParams(HttpServletRequest request) {
        Map srcParamMap = request.getParameterMap();
        HashMap<String, String> destParamMap = new HashMap<String, String>(srcParamMap.size());
        for (Object obj : srcParamMap.keySet()) {
            String[] values = (String[]) srcParamMap.get(obj);
            if (values != null && values.length > 0) {
                destParamMap.put((String) obj, values[0]);
            } else {
                destParamMap.put((String) obj, null);
            }
        }
        return destParamMap;
    }


    private BindingResult doBind(HttpServletRequest webRequest, Class<? extends RestRequest> requestType) {    	
        RestRequest bindObject = BeanUtils.instantiateClass(requestType);
        ServletRequestDataBinder dataBinder = new ServletRequestDataBinder(bindObject, "bindObject");
        dataBinder.setConversionService(getFormattingConversionService());
        dataBinder.setValidator(getValidator());
        dataBinder.bind(webRequest);
        dataBinder.validate();
        return dataBinder.getBindingResult();
    }
    
    private BindingResult doBind(HttpServletRequest webRequest, String content, Class<? extends RestRequest> requestType) {
        RestRequest bindObject = jsonUnmarshaller.unmarshaller(content, requestType);
        ServletRequestDataBinder dataBinder = new ServletRequestDataBinder(bindObject, "bindObject");
        dataBinder.setConversionService(getFormattingConversionService());
        dataBinder.setValidator(getValidator());
        dataBinder.bind(webRequest);
        dataBinder.validate();
        return dataBinder.getBindingResult();
    }

    private Validator getValidator() {
        if (this.validator == null) {
            LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
            localValidatorFactoryBean.afterPropertiesSet();
            this.validator = localValidatorFactoryBean;
        }
        return this.validator;
    }

    public FormattingConversionService getFormattingConversionService() {
        return conversionService;
    }

    //默认的{@link RestRequest}实现类
    private class DefaultRestRequest extends AbstractRestRequest {
    }
}

