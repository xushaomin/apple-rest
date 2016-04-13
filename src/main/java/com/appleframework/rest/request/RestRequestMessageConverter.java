/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-4-17
 */
package com.appleframework.rest.request;

import com.appleframework.rest.RestRequestParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

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
@SuppressWarnings("deprecation")
public class RestRequestMessageConverter implements ConditionalGenericConverter {

	private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        objectMapper.setAnnotationIntrospector(introspector);
		objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
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

