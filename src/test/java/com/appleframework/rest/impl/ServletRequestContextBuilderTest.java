/**
 *  
 * 日    期：12-6-12
 */
package com.appleframework.rest.impl;

import com.appleframework.rest.RestContext;
import com.appleframework.rest.ServiceMethodHandler;
import com.appleframework.rest.config.SystemParameterNames;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class ServletRequestContextBuilderTest {

    @Test
    public void testIpParsed() {
        FormattingConversionService conversionService = mock(FormattingConversionService.class);
        ServletRequestContextBuilder requestContextBuilder = new ServletRequestContextBuilder(conversionService);
        RestContext restContext = mock(RestContext.class);

        //构造HttpServletRequest
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRemoteAddr("1.1.1.1");

        //创建SimpleRequestContext
        SimpleRestRequestContext requestContext = requestContextBuilder.buildBySysParams(restContext, servletRequest,null);
        assertEquals(requestContext.getIp(), "1.1.1.1");

        servletRequest.setRemoteAddr("1.1.1.1");
        servletRequest.addHeader(ServletRequestContextBuilder.X_FORWARDED_FOR, "null,2.2.2.2,3.3.3.3");
        requestContext = requestContextBuilder.buildBySysParams(restContext, servletRequest,null);
        assertEquals(requestContext.getIp(), "2.2.2.2");

        servletRequest.addHeader(ServletRequestContextBuilder.X_REAL_IP, "5.5.5.5");
        requestContext = requestContextBuilder.buildBySysParams(restContext, servletRequest,null);
        assertEquals(requestContext.getIp(), "5.5.5.5");

    }

    /**
     * 正常情况下的系统参数绑定
     *
     * @throws Exception
     */
    @Test
    public void testBuildBySysParams1() throws Exception {
        FormattingConversionService conversionService = mock(FormattingConversionService.class);
        ServletRequestContextBuilder requestContextBuilder = new ServletRequestContextBuilder(conversionService);

        RestContext restContext = mock(RestContext.class);
        ServiceMethodHandler methodHandler = mock(ServiceMethodHandler.class);
        when(restContext.getServiceMethodHandler("method1")).thenReturn(methodHandler);

        //构造HttpServletRequest
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();

        servletRequest.setParameter(SystemParameterNames.getMethod(), "method1");
        servletRequest.setParameter("param1", "value1");
        servletRequest.setParameter("param2", "value2");
        servletRequest.setParameter("param3", "value3");

        //创建SimpleRequestContext
        SimpleRestRequestContext requestContext =
                requestContextBuilder.buildBySysParams(restContext, servletRequest,null);

        assertEquals(requestContext.getAllParams().size(), 10);
        assertEquals(requestContext.getParamValue("param1"), "value1");
        assertEquals(requestContext.getRawRequestObject(), servletRequest);

        assertEquals(requestContext.getMethod(), "method1");

        assertEquals(requestContext.getServiceMethodHandler(), methodHandler);
    }

    /**
     * 看错误的参数是否会被自动转为默认的
     *
     * @throws Exception
     */
    @Test
    public void testBuildBySysParams2() throws Exception {
        FormattingConversionService conversionService = mock(FormattingConversionService.class);
        ServletRequestContextBuilder requestContextBuilder = new ServletRequestContextBuilder(conversionService);
        RestContext restContext = mock(RestContext.class);


        //构造HttpServletRequest
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();


        //创建SimpleRequestContext
        SimpleRestRequestContext requestContext =
                requestContextBuilder.buildBySysParams(restContext, servletRequest,null);
    }

    /**
     * 非{@link javax.servlet.http.HttpServletRequest}
     *
     * @throws Exception
     */
    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testBuildBySysParams3() throws Exception {
        FormattingConversionService conversionService = mock(FormattingConversionService.class);
        ServletRequestContextBuilder requestContextBuilder = new ServletRequestContextBuilder(conversionService);

        RestContext restContext = mock(RestContext.class);
        ServiceMethodHandler methodHandler = mock(ServiceMethodHandler.class);
        when(restContext.getServiceMethodHandler("method1")).thenReturn(methodHandler);

        //创建SimpleRequestContext
        SimpleRestRequestContext requestContext =
                requestContextBuilder.buildBySysParams(restContext, new Object(),null);
    }

    @Test
    public void testBindBusinessParams() throws Exception {

    }
}

