/**
 *
 * 日    期：12-2-27
 */
package com.appleframework.rest.marshaller;

import com.appleframework.rest.RestException;
import com.appleframework.rest.RestMarshaller;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <pre>
 *    将响应对象流化成JSON。 {@link ObjectMapper}是线程安全的。
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class JacksonJsonRestMarshaller implements RestMarshaller {

    private static ObjectMapper objectMapper;

    public void marshaller(Object object, OutputStream outputStream) {
        try {
            JsonGenerator jsonGenerator = getObjectMapper().getJsonFactory().createJsonGenerator(outputStream, JsonEncoding.UTF8);
            getObjectMapper().writeValue(jsonGenerator, object);
        } catch (IOException e) {
            throw new RestException(e);
        }
    }

    private ObjectMapper getObjectMapper() throws IOException {
        if (JacksonJsonRestMarshaller.objectMapper == null) {
            ObjectMapper objectMapper = new ObjectMapper();
            AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
            SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
            serializationConfig = serializationConfig.without(SerializationConfig.Feature.WRAP_ROOT_VALUE)
                    .with(SerializationConfig.Feature.INDENT_OUTPUT)
                    .withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
                    .withSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY)
                    .withAnnotationIntrospector(introspector);
            objectMapper.setSerializationConfig(serializationConfig);
            JacksonJsonRestMarshaller.objectMapper = objectMapper;
        }
        return JacksonJsonRestMarshaller.objectMapper;
    }
}

