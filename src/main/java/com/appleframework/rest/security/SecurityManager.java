/**
 *
 * 日    期：12-2-13
 */
package com.appleframework.rest.security;

import com.appleframework.rest.RestRequestContext;

/**
 * <pre>
 *   负责对请求数据、会话、业务安全、应用密钥安全进行检查并返回相应的错误
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public interface SecurityManager {

    /**
     * 对请求服务的上下文进行检查校验
     *
     * @param restRequestContext
     * @return
     */
    MainError validateSystemParameters(RestRequestContext restRequestContext);

    /**
     * 验证其它的事项：包括业务参数，业务安全性，会话安全等
     *
     * @param restRequestContext
     * @return
     */
    MainError validateOther(RestRequestContext restRequestContext);

    /**
     * 文件上传控制器
     * @param fileUploadController
     */
    void setFileUploadController(FileUploadController fileUploadController);
}

