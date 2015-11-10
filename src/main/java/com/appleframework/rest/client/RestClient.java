/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-29
 */
package com.appleframework.rest.client;

import com.appleframework.rest.request.RestConverter;


/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public interface RestClient {

    /**
     * 添加自定义的转换器
     *
     * @param restConverter
     */
    @SuppressWarnings("rawtypes")
	void addRestConvertor(RestConverter restConverter);


    /**
     * 设置method的参数名
     *
     * @param paramName
     * @return
     */
    RestClient setMethodParamName(String paramName);

    /**
     * 设置content的参数名
     *
     * @param paramName
     * @return
     */
    RestClient setContentParamName(String paramName);

    /**
     * 创建一个新的服务请求
     * @return
     */
    ClientRequest buildClientRequest();
}

