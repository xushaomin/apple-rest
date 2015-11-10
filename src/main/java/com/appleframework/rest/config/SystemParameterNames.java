/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-5
 */
package com.appleframework.rest.config;

/**
 * <pre>
 *   系统级参数的名称
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class SystemParameterNames {

    //方法的默认参数名
    private static final String METHOD = "method";
    
    //格式化默认参数名
    private static final String CONTENT = "content";

    private static String method = METHOD;

    private static String content = CONTENT;

    public static String getMethod() {
        return method;
    }

    public static void setMethod(String method) {
        SystemParameterNames.method = method;
    }

    public static String getContent() {
        return content;
    }

    public static void setContent(String content) {
        SystemParameterNames.content = content;
    }

}
