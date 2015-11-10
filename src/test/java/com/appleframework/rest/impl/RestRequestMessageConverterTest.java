/**
 *  
 * 日    期：12-5-29
 */
package com.appleframework.rest.impl;

import com.appleframework.rest.request.RestRequestMessageConverter;
import org.springframework.core.convert.TypeDescriptor;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


/**
 * @author 陈雄华
 * @version 1.0
 */
public class RestRequestMessageConverterTest {

    @Test
    public void testConvertOfXmlFormat() throws Exception {
        TypeDescriptor addrTypeDescriptor = TypeDescriptor.valueOf(Addresss.class);
        TypeDescriptor strTypeDescriptor = TypeDescriptor.valueOf(String.class);
        RestRequestMessageConverter converter = new RestRequestMessageConverter();
        String addressStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<address zoneCode=\"001\" doorCode=\"002\">\n" +
                "  <streets>\n" +
                "    <street no=\"001\" name=\"street1\"/>\n" +
                "    <street no=\"002\" name=\"street2\"/>\n" +
                "  </streets>\n" +
                "</address>";
        Object destObj = converter.convert(addressStr, strTypeDescriptor, addrTypeDescriptor);
        assertTrue(destObj instanceof Addresss);
        Addresss addresss = (Addresss) destObj;
        assertEquals(addresss.getZoneCode(), "001");
        assertEquals(addresss.getDoorCode(), "002");
        assertEquals(addresss.getStreets().size(), 2);
    }

    @Test
    public void testConvertOfJsonFormat() throws Exception {
        TypeDescriptor addrTypeDescriptor = TypeDescriptor.valueOf(Addresss.class);
        TypeDescriptor strTypeDescriptor = TypeDescriptor.valueOf(String.class);
        RestRequestMessageConverter converter = new RestRequestMessageConverter();
        String addressStr = "{\"zoneCode\":\"001\",\"doorCode\":\"002\",\"streets\":[{\"no\":\"001\",\"name\":\"street1\"}]}";
        Object destObj = converter.convert(addressStr, strTypeDescriptor, addrTypeDescriptor);
        assertTrue(destObj instanceof Addresss);
        Addresss addresss = (Addresss) destObj;
        assertEquals(addresss.getZoneCode(), "001");
        assertEquals(addresss.getDoorCode(), "002");
        assertNotNull(addresss.getStreets());
        assertEquals(addresss.getStreets().size(), 1);
    }
}

