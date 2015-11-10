/**
 *
 * 日    期：12-2-27
 */
package com.appleframework.rest.impl;

import com.appleframework.rest.*;
import com.appleframework.rest.annotation.HttpAction;
import com.appleframework.rest.security.MainError;
import com.appleframework.rest.utils.RestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 陈雄华
 * @version 1.0
 */
public class SimpleRestRequestContext implements RestRequestContext {

    public static final String SPRING_VALIDATE_ERROR_ATTRNAME = "$SPRING_VALIDATE_ERROR_ATTRNAME";

    private RestContext restContext;

    private String method;

    private String content;

    private Map<String, Object> attributes = new HashMap<String, Object>();

    private ServiceMethodHandler serviceMethodHandler;

    private MainError mainError;

    private Object restResponse;

    private long serviceBeginTime = -1;

    private long serviceEndTime = -1;

    private String ip;

    private HttpAction httpAction;

    private Object rawRequestObject;

    private Object rawResponseObject;

    private Map<String, String> allParams;

    private String requestId = RestUtils.getUUID();

    @Override
    public long getServiceBeginTime() {
        return this.serviceBeginTime;
    }

    @Override
    public long getServiceEndTime() {
        return this.serviceEndTime;
    }

    @Override
    public void setServiceBeginTime(long serviceBeginTime) {
        this.serviceBeginTime = serviceBeginTime;
    }

    @Override
    public void setServiceEndTime(long serviceEndTime) {
        this.serviceEndTime = serviceEndTime;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public Object getRawRequestObject() {
        return this.rawRequestObject;
    }

    @Override
    public Object getRawResponseObject() {
        return this.rawResponseObject;
    }

    public void setRawRequestObject(Object rawRequestObject) {
        this.rawRequestObject = rawRequestObject;
    }

    public void setRawResponseObject(Object rawResponseObject) {
        this.rawResponseObject = rawResponseObject;
    }

    public SimpleRestRequestContext(RestContext restContext) {
        this.restContext = restContext;
        this.serviceBeginTime = System.currentTimeMillis();
    }


    @Override
    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public RestContext getRestContext() {
        return restContext;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public ServiceMethodHandler getServiceMethodHandler() {
        return this.serviceMethodHandler;
    }

    @Override
    public Object getRestResponse() {
        return this.restResponse;
    }

    public void setServiceMethodHandler(ServiceMethodHandler serviceMethodHandler) {
        this.serviceMethodHandler = serviceMethodHandler;
    }

    @Override
    public void setRestResponse(Object restResponse) {
        this.restResponse = restResponse;
    }

    public void setMainError(MainError mainError) {
        this.mainError = mainError;
    }

    public MainError getMainError() {
        return this.mainError;
    }

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public ServiceMethodDefinition getServiceMethodDefinition() {
        return serviceMethodHandler.getServiceMethodDefinition();
    }

    @Override
    public Map<String, String> getAllParams() {
        return this.allParams;
    }

    public void setAllParams(Map<String, String> allParams) {
        this.allParams = allParams;
    }

    @Override
    public String getParamValue(String paramName) {
        if (allParams != null) {
            return allParams.get(paramName);
        } else {
            return null;
        }
    }

    public void setHttpAction(HttpAction httpAction) {
        this.httpAction = httpAction;
    }

    @Override
    public HttpAction getHttpAction() {
        return this.httpAction;
    }

    @Override
    public String getRequestId() {
        return this.requestId;
    }
}

