/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-30
 */
package com.appleframework.rest.client.unmarshaller;

import com.appleframework.rest.RestException;
import com.appleframework.rest.client.RestUnmarshaller;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import java.io.IOException;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class JacksonJsonRestUnmarshaller implements RestUnmarshaller {

    private static ObjectMapper objectMapper;

    @Override
    public <T> T unmarshaller(String content, Class<T> objectType) {
        try {
            return getObjectMapper().readValue(content, objectType);
        } catch (IOException e) {
            throw new RestException(e);
        }
    }

    @SuppressWarnings("deprecation")
	private ObjectMapper getObjectMapper() throws IOException {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();            
            AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
            objectMapper.setAnnotationIntrospector(introspector);
    		objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        }
        return objectMapper;
    }
}

