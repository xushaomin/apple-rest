/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-5
 */
package com.appleframework.rest.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * <pre>
 *    指定自定义的系统参数名
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class SystemParameterNamesBeanDefinitionParser implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        
        String method = element.getAttribute("method-param-name");
        String content = element.getAttribute("content-param-name");

        if (StringUtils.hasText(content)) {
            SystemParameterNames.setContent(content);
        }
        if (StringUtils.hasText(method)) {
            SystemParameterNames.setMethod(method);
        }
        return null;
    }
}

