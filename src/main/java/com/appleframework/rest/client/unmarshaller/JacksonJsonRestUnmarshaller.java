/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-30
 */
package com.appleframework.rest.client.unmarshaller;

import com.appleframework.rest.RestException;
import com.appleframework.rest.client.RestUnmarshaller;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

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

    private ObjectMapper getObjectMapper() throws IOException {
        if (JacksonJsonRestUnmarshaller.objectMapper == null) {
            ObjectMapper objectMapper = new ObjectMapper();
            AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
            SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
            serializationConfig = serializationConfig.without(SerializationConfig.Feature.WRAP_ROOT_VALUE)
                                                     .withAnnotationIntrospector(introspector);
            objectMapper.setSerializationConfig(serializationConfig);
            JacksonJsonRestUnmarshaller.objectMapper = objectMapper;
        }
        return JacksonJsonRestUnmarshaller.objectMapper;
    }
}

