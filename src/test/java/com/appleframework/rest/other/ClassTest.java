/**
 *  
 * 日    期：12-6-6
 */
package com.appleframework.rest.other;

import com.appleframework.rest.AbstractRestRequest;
import com.appleframework.rest.RestRequest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class ClassTest {

    @Test
    public void testAssignableFrom() {
        assertTrue(!MyRestRequest.class.isAssignableFrom(RestRequest.class));
        assertTrue(!MyRestRequest.class.isAssignableFrom(AbstractRestRequest.class));
        assertTrue(AbstractRestRequest.class.isAssignableFrom(MyRestRequest.class));
    }

    @Test
    public void modeInt() {
        int len = 16 - 1;
        for (int i = 0; i < 100; i++) {
            int i1 = i & len;
            System.out.println("i:" + i1);
            assertTrue(i1 <= len);
        }
    }

    private class MyRestRequest extends AbstractRestRequest {

    }

}

