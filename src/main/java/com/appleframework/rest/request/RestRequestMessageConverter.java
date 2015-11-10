/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-4-17
 */
package com.appleframework.rest.request;

import com.appleframework.rest.RestRequestParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.Set;

/**
 * <pre>
 *     将参数中的XML或JSON转换为对象的属性对象
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class RestRequestMessageConverter implements ConditionalGenericConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
        serializationConfig = serializationConfig.without(SerializationConfig.Feature.WRAP_ROOT_VALUE)
                                                 .withAnnotationIntrospector(introspector);
        objectMapper.setSerializationConfig(serializationConfig);
    }


    /**
     * 如果目标属性类有标注JAXB的注解，则使用该转换器
     *
     * @param sourceType
     * @param targetType
     * @return
     */
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        Class<?> clazz = targetType.getObjectType();
        return clazz.isAnnotationPresent(XmlRootElement.class) || clazz.isAnnotationPresent(XmlType.class);
    }

    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Object.class));
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        try {
        	JsonParser jsonParser = objectMapper.getJsonFactory().createJsonParser((String) source);
        	return jsonParser.readValueAs(targetType.getObjectType());
        } catch (Exception e) {
            throw new RestRequestParseException((String) source, e);
        }
    }

}

