/**
 *
 * 日    期：12-2-11
 */
package com.appleframework.rest.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.util.Assert;

import java.util.Locale;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class MainErrors {

    protected static Logger logger = LoggerFactory.getLogger(MainErrors.class);

    private static final String ERROR_CODE_PREFIX = "ERROR_";

    // 错误信息的国际化信息
    private static MessageSourceAccessor errorMessageSourceAccessor;

    public static MainError getError(MainErrorType mainErrorType, Object... params) {
    	Locale locale = Locale.CHINA;
        String errorMessage = getErrorMessage(ERROR_CODE_PREFIX + mainErrorType.value(),locale,params);
        return new SimpleMainError(Integer.parseInt(mainErrorType.value()), errorMessage);
    }

    public static void setErrorMessageSourceAccessor(MessageSourceAccessor errorMessageSourceAccessor) {
        MainErrors.errorMessageSourceAccessor = errorMessageSourceAccessor;
    }

    private static String getErrorMessage(String code, Object... params) {
        try {
        	Locale locale = Locale.CHINA;
            Assert.notNull(errorMessageSourceAccessor, "请先设置错误消息的国际化资源");            	
            return errorMessageSourceAccessor.getMessage(code, params, locale);
        } catch (NoSuchMessageException e) {
            logger.error("不存在对应的错误键：{}，请检查是否在i18n/rest/error的错误资源", code);
            throw e;
        }
    }

}

