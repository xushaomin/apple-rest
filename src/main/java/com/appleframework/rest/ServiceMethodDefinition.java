/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-5-31
 */
package com.appleframework.rest;


import com.appleframework.rest.annotation.HttpAction;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class ServiceMethodDefinition {

    /**
     * 默认的组
     */
    public static final String DEFAULT_GROUP = "DEFAULT";

    /**
     * 默认分组标识
     */
    public static final String DEFAULT_GROUP_TITLE = "DEFAULT GROUP";

    /**
     * API的方法
     */
    private String method;

    /**
     * API的方法的标识
     */
    private String methodTitle;

    /**
     * HTTP请求的方法
     */
    private HttpAction[] httpAction;

    /**
     * API方法所属组名
     */
    private String methodGroup = DEFAULT_GROUP;

    /**
     * API方法组名的标识
     */
    private String methodGroupTitle;

    /**
     * API所属的标签
     */
    private String[] tags = {};

    /**
     * 过期时间，单位为秒，0或负数表示不过期
     */
    private int timeout = -9999;


    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethodTitle() {
        return methodTitle;
    }

    public void setMethodTitle(String methodTitle) {
        this.methodTitle = methodTitle;
    }

    public String getMethodGroup() {
        return methodGroup;
    }

    public void setMethodGroup(String methodGroup) {
        this.methodGroup = methodGroup;
    }

    public String getMethodGroupTitle() {
        return methodGroupTitle;
    }

    public void setMethodGroupTitle(String methodGroupTitle) {
        this.methodGroupTitle = methodGroupTitle;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public HttpAction[] getHttpAction() {
        return httpAction;
    }

    public void setHttpAction(HttpAction[] httpAction) {
        this.httpAction = httpAction;
    }

}

