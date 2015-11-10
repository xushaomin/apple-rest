/**
 *
 * 日    期：12-2-23
 */
package com.appleframework.rest.security;

import java.util.EnumMap;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public enum MainErrorType {
    SERVICE_CURRENTLY_UNAVAILABLE,
    UPLOAD_FAIL,
    HTTP_ACTION_NOT_ALLOWED,
    INVALID_ENCODING,
    FORBIDDEN_REQUEST,
    METHOD_OBSOLETED,
    BUSINESS_LOGIC_ERROR,
    MISSING_METHOD,
    INVALID_METHOD,
    MISSING_REQUIRED_ARGUMENTS,
    INVALID_ARGUMENTS,
    EXCEED_USER_INVOKE_LIMITED,
    EXCEED_SESSION_INVOKE_LIMITED,
    EXCEED_APP_INVOKE_LIMITED,
    EXCEED_APP_INVOKE_FREQUENCY_LIMITED;

    private static EnumMap<MainErrorType, String> errorCodeMap = new EnumMap<MainErrorType, String>(MainErrorType.class);

    static {
        errorCodeMap.put(MainErrorType.SERVICE_CURRENTLY_UNAVAILABLE, "1");
        errorCodeMap.put(MainErrorType.UPLOAD_FAIL, "4");
        errorCodeMap.put(MainErrorType.HTTP_ACTION_NOT_ALLOWED, "5");
        errorCodeMap.put(MainErrorType.INVALID_ENCODING, "6");
        errorCodeMap.put(MainErrorType.FORBIDDEN_REQUEST, "7");
        errorCodeMap.put(MainErrorType.METHOD_OBSOLETED, "8");
        errorCodeMap.put(MainErrorType.BUSINESS_LOGIC_ERROR, "9");
        errorCodeMap.put(MainErrorType.MISSING_METHOD, "26");
        errorCodeMap.put(MainErrorType.INVALID_METHOD, "27");
        errorCodeMap.put(MainErrorType.MISSING_REQUIRED_ARGUMENTS, "32");
        errorCodeMap.put(MainErrorType.INVALID_ARGUMENTS, "33");
        errorCodeMap.put(MainErrorType.EXCEED_USER_INVOKE_LIMITED, "34");
        errorCodeMap.put(MainErrorType.EXCEED_SESSION_INVOKE_LIMITED, "35");
        errorCodeMap.put(MainErrorType.EXCEED_APP_INVOKE_LIMITED, "36");
        errorCodeMap.put(MainErrorType.EXCEED_APP_INVOKE_FREQUENCY_LIMITED, "37");
    }

    public String value() {
        return errorCodeMap.get(this);
    }
}

