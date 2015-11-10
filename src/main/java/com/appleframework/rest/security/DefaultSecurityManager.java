/**
 *
 * 日    期：12-2-13
 */
package com.appleframework.rest.security;

import com.appleframework.rest.*;
import com.appleframework.rest.annotation.HttpAction;
import com.appleframework.rest.config.SystemParameterNames;
import com.appleframework.rest.impl.SimpleRestRequestContext;
import com.appleframework.rest.request.UploadFileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.*;

/**
 * @author 陈雄华
 * @version 1.0
 */
public class DefaultSecurityManager implements SecurityManager {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected FileUploadController fileUploadController;

    private static final Map<String, SubErrorType> INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS = new LinkedHashMap<String, SubErrorType>();

    static {
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("typeMismatch", SubErrorType.ISV_PARAMETERS_MISMATCH);
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("NotNull", SubErrorType.ISV_MISSING_PARAMETER);
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("NotEmpty", SubErrorType.ISV_INVALID_PARAMETE);
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("Size", SubErrorType.ISV_INVALID_PARAMETE);
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("Range", SubErrorType.ISV_INVALID_PARAMETE);
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("Pattern", SubErrorType.ISV_INVALID_PARAMETE);
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("Min", SubErrorType.ISV_INVALID_PARAMETE);
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("Max", SubErrorType.ISV_INVALID_PARAMETE);
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("DecimalMin", SubErrorType.ISV_INVALID_PARAMETE);
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("DecimalMax", SubErrorType.ISV_INVALID_PARAMETE);
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("Digits", SubErrorType.ISV_INVALID_PARAMETE);
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("Past", SubErrorType.ISV_INVALID_PARAMETE);
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("Future", SubErrorType.ISV_INVALID_PARAMETE);
        INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.put("AssertFalse", SubErrorType.ISV_INVALID_PARAMETE);
    }


    public MainError validateSystemParameters(RestRequestContext context) {
        RestContext restContext = context.getRestContext();
        MainError mainError = null;

        //3.检查method参数
        if (context.getMethod() == null) {
            return MainErrors.getError(MainErrorType.MISSING_METHOD, SystemParameterNames.getMethod());
        } else {
            if (!restContext.isValidMethod(context.getMethod())) {
                return MainErrors.getError(MainErrorType.INVALID_METHOD,context.getMethod());
            }
        }
        
        //7.检查请求HTTP方法的匹配性
        mainError = validateHttpAction(context);
        if (mainError != null) {
            return mainError;
        }
        return null;
    }


    public MainError validateOther(RestRequestContext rrctx) {

        MainError mainError = null;

        //3.如果是上传文件的服务，检查文件类型和大小是否满足要求
        mainError = checkUploadFile(rrctx);
        if (mainError != null) {
            return mainError;
        }

        //4.检查业务参数合法性
        mainError = validateBusinessParams(rrctx);
        if (mainError != null) {
            return mainError;
        }

        return null;
    }

    private MainError checkUploadFile(RestRequestContext rrctx) {
        ServiceMethodHandler serviceMethodHandler = rrctx.getServiceMethodHandler();
        if (serviceMethodHandler != null && serviceMethodHandler.hasUploadFiles()) {
            List<String> fileFieldNames = serviceMethodHandler.getUploadFileFieldNames();
            for (String fileFieldName : fileFieldNames) {
                String paramValue = rrctx.getParamValue(fileFieldName);
                if (paramValue != null) {
                    if (paramValue.indexOf("@") < 0) {
                        return MainErrors.getError(
                                MainErrorType.UPLOAD_FAIL, 
                                rrctx.getMethod(), "MESSAGE_VALID:not contain '@'.");
                    } else {
                        String fileType = UploadFileUtils.getFileType(paramValue);
                        if (!fileUploadController.isAllowFileType(fileType)) {
                            return MainErrors.getError(
                                    MainErrorType.UPLOAD_FAIL,
                                    rrctx.getMethod(),
                                    "FILE_TYPE_NOT_ALLOW:the valid file types is:" + fileUploadController.getAllowFileTypes());
                        }
                        byte[] fileContent = UploadFileUtils.decode(paramValue);
                        if (fileUploadController.isExceedMaxSize(fileContent.length)) {
                            return MainErrors.getError(
                                    MainErrorType.UPLOAD_FAIL,
                                    rrctx.getMethod(),
                                    "EXCEED_MAX_SIZE:" + fileUploadController.getMaxSize() + "k");
                        }
                    }
                }
            }
        }
        return null;
    }

    public void setFileUploadController(FileUploadController fileUploadController) {
        this.fileUploadController = fileUploadController;
    }

    /**
     * 校验是否是合法的HTTP动作
     *
     * @param restRequestContext
     */
    private MainError validateHttpAction(RestRequestContext restRequestContext) {
        MainError mainError = null;
        HttpAction[] httpActions = restRequestContext.getServiceMethodDefinition().getHttpAction();
        if (httpActions.length > 0) {
            boolean isValid = false;
            for (HttpAction httpAction : httpActions) {
                if (httpAction == restRequestContext.getHttpAction()) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                mainError = MainErrors.getError(
                        MainErrorType.HTTP_ACTION_NOT_ALLOWED,
                        restRequestContext.getMethod(),
                        restRequestContext.getHttpAction());
            }
        }
        return mainError;
    }
    
    @SuppressWarnings("unchecked")
	private MainError validateBusinessParams(RestRequestContext context) {
        List<ObjectError> errorList =
                (List<ObjectError>) context.getAttribute(SimpleRestRequestContext.SPRING_VALIDATE_ERROR_ATTRNAME);

        //将Bean数据绑定时产生的错误转换为Rest的错误
        if (errorList != null && errorList.size() > 0) {
            return toMainErrorOfSpringValidateErrors(errorList, context);
        } else {
            return null;
        }
    }

    /**
     * 将通过JSR 303框架校验的错误转换为Rest的错误体系
     *
     * @param allErrors
     * @param locale
     * @return
     */
    private MainError toMainErrorOfSpringValidateErrors(
            List<ObjectError> allErrors,RestRequestContext context) {
        if (hastSubErrorType(allErrors, SubErrorType.ISV_MISSING_PARAMETER)) {
            return getBusinessParameterMainError(allErrors, SubErrorType.ISV_MISSING_PARAMETER,context);
        } else if (hastSubErrorType(allErrors, SubErrorType.ISV_PARAMETERS_MISMATCH)) {
            return getBusinessParameterMainError(allErrors, SubErrorType.ISV_PARAMETERS_MISMATCH,context);
        } else {
            return getBusinessParameterMainError(allErrors, SubErrorType.ISV_INVALID_PARAMETE,context);
        }
    }

    /**
     * 判断错误列表中是否包括指定的子错误
     *
     * @param allErrors
     * @param subErrorType1
     * @return
     */
    private boolean hastSubErrorType(List<ObjectError> allErrors, SubErrorType subErrorType1) {
        for (ObjectError objectError : allErrors) {
            if (objectError instanceof FieldError) {
                FieldError fieldError = (FieldError) objectError;
                if (INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.containsKey(fieldError.getCode())) {
                    SubErrorType tempSubErrorType = INVALIDE_CONSTRAINT_SUBERROR_MAPPINGS.get(fieldError.getCode());
                    if (tempSubErrorType == subErrorType1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 生成对应子错误的错误类
     *
     * @param allErrors
     * @param locale
     * @param subErrorType
     * @return
     */
    private MainError getBusinessParameterMainError(
            List<ObjectError> allErrors, SubErrorType subErrorType,RestRequestContext context) {
        MainError mainError = SubErrors.getMainError(subErrorType,context.getMethod());
        return mainError;
    }
}

