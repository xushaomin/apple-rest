/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-30
 */
package com.appleframework.rest.client.unmarshaller;

import com.appleframework.rest.RestException;
import com.appleframework.rest.client.RestUnmarshaller;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class JaxbXmlRestUnmarshaller implements RestUnmarshaller {

	private static Map<Class<?>, JAXBContext> jaxbContextHashMap = new ConcurrentHashMap<Class<?>, JAXBContext>();

	@Override
    public <T> T unmarshaller(String content, Class<T> objectType) {
        try {
            Unmarshaller unmarshaller = buildUnmarshaller(objectType);
            StringReader reader = new StringReader(content);
            new InputSource(reader);
            return (T) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RestException(e);
        }

    }

    private Unmarshaller buildUnmarshaller(Class<?> objectType) throws JAXBException {
        if (!jaxbContextHashMap.containsKey(objectType)) {
            JAXBContext context = JAXBContext.newInstance(objectType);
            jaxbContextHashMap.put(objectType, context);
        }
        JAXBContext context = jaxbContextHashMap.get(objectType);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        // unmarshaller.setPresterty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        // unmarshaller.setPresterty(Marshaller.JAXB_ENCODING, "utf-8");
        return unmarshaller;
    }
}

