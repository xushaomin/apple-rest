/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-30
 */
package com.appleframework.rest.client;

import com.appleframework.rest.CommonConstant;
import com.appleframework.rest.MessageFormat;
import com.appleframework.rest.RestRequest;
import com.appleframework.rest.annotation.Temporary;
import com.appleframework.rest.client.unmarshaller.JacksonJsonRestUnmarshaller;
import com.appleframework.rest.config.SystemParameterNames;
import com.appleframework.rest.marshaller.MessageMarshallerUtils;
import com.appleframework.rest.request.RestConverter;
import com.appleframework.rest.request.UploadFile;
import com.appleframework.rest.request.UploadFileConverter;
import com.appleframework.rest.response.ErrorResponse;
import com.appleframework.rest.utils.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.*;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class DefaultRestClient implements RestClient {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    //服务地址
    private String serverUrl;

    private String method;

    private RestTemplate restTemplate = new RestTemplate();

    private RestUnmarshaller jsonUnmarshaller = new JacksonJsonRestUnmarshaller();

    //请求类所有请求参数
    private Map<Class<?>, List<Field>> requestAllFields = new HashMap<Class<?>, List<Field>>();

    //请求类所有不需要进行签名的参数
    private Map<Class<?>, List<String>> requestIgnoreSignFieldNames = new HashMap<Class<?>, List<String>>();


    //键为转换的目标类型
    private static Map<Class<?>, RestConverter<String, ?>> restConverterMap =
            new HashMap<Class<?>, RestConverter<String, ?>>();
    {
        restConverterMap.put(UploadFile.class, new UploadFileConverter());
    }

    public DefaultRestClient(String serverUrl, String method, String appSecret) {
        this.serverUrl = serverUrl;
        this.method = method;
    }

   

    @Override
    public RestClient setMethodParamName(String paramName) {
        SystemParameterNames.setMethod(paramName);
        return this;
    }

    @Override
    public void addRestConvertor(RestConverter restConverter) {
        this.restConverterMap.put(restConverter.getTargetClass(), restConverter);
    }

    @Override
    public ClientRequest buildClientRequest() {
        return new DefaultClientRequest(this);
    }

    private class DefaultClientRequest implements ClientRequest {

        private RestClient restClient;

        private Map<String, String> paramMap = new HashMap<String, String>(20);

        private List<String> ignoreSignParams = new ArrayList<String>();

        private DefaultClientRequest(RestClient restClient) {
            this.restClient = restClient;
        }

        @Override
        public ClientRequest addParam(String paramName, Object paramValue) {
            addParam(paramName,paramValue,false);
            return this;
        }

        @Override
        public ClientRequest clearParam() {
            paramMap.clear();
            return this;
        }

        @Override
        public ClientRequest addParam(String paramName, Object paramValue, boolean ignoreSign) {
            Assert.isTrue(paramName != null && paramName.length() > 0, "参数名不能为空");
            Assert.notNull(paramValue, "参数值不能为null");

            //将参数添加到参数列表中
            String valueAsStr = paramValue.toString();
            if (restConverterMap.containsKey(paramValue.getClass())) {
                RestConverter restConverter = restConverterMap.get(paramValue.getClass());
                valueAsStr = (String) restConverter.unconvert(paramValue);
            }
            paramMap.put(paramName, valueAsStr);
            return this;
        }

        @Override
        public <T> CompositeResponse post(Class<T> restResponseClass, String methodName, String version) {
            Map<String, String> requestParams = addOtherParamMap(methodName, version);
            return post(restResponseClass, requestParams);
        }

        @Override
        public <T> CompositeResponse post(RestRequest restRequest, Class<T> restResponseClass, String methodName, String version) {
            Map<String, String> requestParams = getRequestForm(restRequest, methodName, version);
            return post(restResponseClass, requestParams);
        }

        private <T> CompositeResponse post(Class<T> restResponseClass, Map<String, String> requestParams) {
            String responseContent = restTemplate.postForObject(serverUrl, toMultiValueMap(requestParams), String.class);
            if (logger.isDebugEnabled()) {
                logger.debug("response:\n" + responseContent);
            }
            return toCompositeResponse(responseContent, restResponseClass);
        }

        @Override
        public <T> CompositeResponse get(Class<T> restResponseClass, String methodName, String version) {
            Map<String, String> requestParams = addOtherParamMap(methodName, version);
            return get(restResponseClass, requestParams);
        }

        @Override
        public <T> CompositeResponse get(RestRequest restRequest, Class<T> restResponseClass, String methodName, String version) {
            Map<String, String> requestParams = getRequestForm(restRequest, methodName, version);
            return get(restResponseClass, requestParams);
        }

        private <T> CompositeResponse get(Class<T> restResponseClass, Map<String, String> requestParams) {
            String responseContent = restTemplate.getForObject(buildGetUrl(requestParams), String.class, requestParams);
            if (logger.isDebugEnabled()) {
                logger.debug("response:\n" + responseContent);
            }
            return toCompositeResponse(responseContent, restResponseClass);
        }

        private Map<String, String> addOtherParamMap(String methodName, String version) {
            paramMap.put(SystemParameterNames.getMethod(), methodName);
            return paramMap;
        }

        private <T> CompositeResponse toCompositeResponse(String content, Class<T> restResponseClass) {
            if(logger.isDebugEnabled()){
                logger.debug(content);
            }
            boolean successful = isSuccessful(content);
            DefaultCompositeResponse<T> compositeResponse = new DefaultCompositeResponse<T>(successful);

                if (successful) {
                    T restResponse = jsonUnmarshaller.unmarshaller(content, restResponseClass);
                    compositeResponse.setSuccessRestResponse(restResponse);
                } else {
                    ErrorResponse errorResponse = jsonUnmarshaller.unmarshaller(content, ErrorResponse.class);
                    compositeResponse.setErrorResponse(errorResponse);
                }
            return compositeResponse;
        }

        private boolean isSuccessful(String content) {
            return !(content.contains(CommonConstant.ERROR_CODE));
        }

        private String buildGetUrl(Map<String, String> form) {
            StringBuilder requestUrl = new StringBuilder();
            requestUrl.append(serverUrl);
            requestUrl.append("?");
            String joinChar = "";
            for (Map.Entry<String, String> entry : form.entrySet()) {
                requestUrl.append(joinChar);
                requestUrl.append(entry.getKey());
                requestUrl.append("=");
                requestUrl.append(entry.getValue());
                joinChar = "&";
            }
            return requestUrl.toString();
        }

        private Map<String, String> getRequestForm(RestRequest restRequest, String methodName, String version) {

            Map<String, String> form = new LinkedHashMap<String, String>(16);

            //系统级参数
            form.put(SystemParameterNames.getMethod(), methodName);
            return form;
        }

        private MultiValueMap<String, String> toMultiValueMap(Map<String, String> form) {
            MultiValueMap<String, String> mvm = new LinkedMultiValueMap<String, String>();
            for (Map.Entry<String, String> entry : form.entrySet()) {
                mvm.add(entry.getKey(), entry.getValue());
            }
            return mvm;
        }

        /**
         * 对请求参数进行签名
         *
         * @param restRequestClass
         * @param appSecret
         * @param form
         * @return
         */
        private String sign(Class<?> restRequestClass, String appSecret, Map<String, String> form) {
            List<String> ignoreFieldNames = requestIgnoreSignFieldNames.get(restRequestClass);
            return RestUtils.sign(form, ignoreFieldNames, appSecret);
        }

        /**
         * 获取restRequest对应的参数名列表
         *
         * @param restRequest
         * @param mf
         * @return
         */
        private Map<String, String> getParamFields(RestRequest restRequest, MessageFormat mf) {
            if (!requestAllFields.containsKey(restRequest.getClass())) {
                parseRestRequestClass(restRequest);
            }
            return toParamValueMap(restRequest, mf);
        }

        /**
         * 获取restRequest对象的对应的参数列表
         *
         * @param restRequest
         * @param mf
         * @return
         */
        private Map<String, String> toParamValueMap(RestRequest restRequest, MessageFormat mf) {
            List<Field> fields = requestAllFields.get(restRequest.getClass());
            Map<String, String> params = new HashMap<String, String>();
            for (Field field : fields) {
                RestConverter convertor = getConvertor(field.getType());
                Object fieldValue = ReflectionUtils.getField(field, restRequest);
                if (fieldValue != null) {
                    if (convertor != null) {//有对应转换器
                        String strParamValue = (String) convertor.unconvert(fieldValue);
                        params.put(field.getName(), strParamValue);
                    } else if (field.getType().isAnnotationPresent(XmlRootElement.class) ||
                            field.getType().isAnnotationPresent(XmlType.class)) {
                        String message = MessageMarshallerUtils.getMessage(fieldValue, mf);
                        params.put(field.getName(), message);
                    } else {
                        params.put(field.getName(), fieldValue.toString());
                    }
                }
            }
            return params;
        }
    }

    private RestConverter getConvertor(Class<?> fieldType) {
        for (Class<?> aClass : restConverterMap.keySet()) {
            if (ClassUtils.isAssignable(aClass, fieldType)) {
                return restConverterMap.get(aClass);
            }
        }
        return null;
    }

    private void parseRestRequestClass(RestRequest restRequest) {
        final ArrayList<Field> allFields = new ArrayList<Field>();
        ReflectionUtils.doWithFields(restRequest.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                ReflectionUtils.makeAccessible(field);
                if (!isTemporaryField(field)) {
                    allFields.add(field);
                }
            }

            private boolean isTemporaryField(Field field) {
                Annotation[] declaredAnnotations = field.getDeclaredAnnotations();
                if (declaredAnnotations != null) {
                    for (Annotation declaredAnnotation : declaredAnnotations) {
                        Temporary varTemporary = field.getAnnotation(Temporary.class);
                        if (varTemporary != null) {
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        requestAllFields.put(restRequest.getClass(), allFields);
    }

    /**
     * 获取restRequest对应的参数名列表
     *
     * @param restRequest
     * @param mf
     * @return
     */
    private Map<String, String> getParamFields(RestRequest restRequest, MessageFormat mf) {
        if (!requestAllFields.containsKey(restRequest.getClass())) {
            parseRestRequestClass(restRequest);
        }
        return toParamValueMap(restRequest, mf);
    }

    /**
     * 获取restRequest对象的对应的参数列表
     *
     * @param restRequest
     * @param mf
     * @return
     */
    private Map<String, String> toParamValueMap(RestRequest restRequest, MessageFormat mf) {
        List<Field> fields = requestAllFields.get(restRequest.getClass());
        Map<String, String> params = new HashMap<String, String>();
        for (Field field : fields) {
            RestConverter convertor = getConvertor(field.getType());
            Object fieldValue = ReflectionUtils.getField(field, restRequest);
            if (fieldValue != null) {
                if (convertor != null) {//有对应转换器
                    String strParamValue = (String) convertor.unconvert(fieldValue);
                    params.put(field.getName(), strParamValue);
                } else if (field.getType().isAnnotationPresent(XmlRootElement.class) ||
                        field.getType().isAnnotationPresent(XmlType.class)) {
                    String message = MessageMarshallerUtils.getMessage(fieldValue, mf);
                    params.put(field.getName(), message);
                } else {
                    params.put(field.getName(), fieldValue.toString());
                }
            }
        }
        return params;
    }



	@Override
	public RestClient setContentParamName(String paramName) {
		return null;
	}


}

