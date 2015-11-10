/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-4
 */
package com.appleframework.rest.config;

import com.appleframework.rest.ThreadFerry;
import com.appleframework.rest.impl.AnnotationServletServiceRouterFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

    public static final int DEFAULT_CORE_POOL_SIZE = 200;
    public static final int DEFAULT_MAX_POOL_SIZE = 500;
    public static final int DEFAULT_KEEP_ALIVE_SECONDS = 5 * 60;
    public static final int DEFAULT_QUENE_CAPACITY = 20;
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        Object source = parserContext.extractSource(element);
        CompositeComponentDefinition compDefinition = new CompositeComponentDefinition(element.getTagName(), source);
        parserContext.pushContainingComponent(compDefinition);

        //注册ServiceRouter Bean
        RootBeanDefinition serviceRouterDef = new RootBeanDefinition(AnnotationServletServiceRouterFactoryBean.class);
        String serviceRouterName = element.getAttribute("id");
        if (StringUtils.hasText(serviceRouterName)) {
            parserContext.getRegistry().registerBeanDefinition(serviceRouterName, serviceRouterDef);
        } else {
            serviceRouterName = parserContext.getReaderContext().registerWithGeneratedName(serviceRouterDef);
        }
        parserContext.registerComponent(new BeanComponentDefinition(serviceRouterDef, serviceRouterName));

        //设置TaskExecutor
        setTaskExecutor(element, parserContext, source, serviceRouterDef);

        //设置signEnable
        setSignEnable(element, serviceRouterDef);

        //设置threadFerryClass
        setThreadFerry(element, serviceRouterDef);

        //设置国际化错误文件
        setExtErrorBaseNames(element, serviceRouterDef);

        //设置服务过期时间
        setServiceTimeout(element, serviceRouterDef);

        //设置文件上传配置信息
        setUploadFileSetting(element, serviceRouterDef);

        parserContext.popAndRegisterContainingComponent();
        return null;
    }

    private void setUploadFileSetting(Element element, RootBeanDefinition serviceRouterDef) {
        String uploadFileMaxSize = element.getAttribute("upload-file-max-size");
        if (StringUtils.hasText(uploadFileMaxSize)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Rest配置文件最大上传大小为{}K",uploadFileMaxSize);
            }
            serviceRouterDef.getPropertyValues().addPropertyValue("uploadFileMaxSize",uploadFileMaxSize);
        }

        String uploadFileTypes = element.getAttribute("upload-file-types");
        if (StringUtils.hasText(uploadFileTypes)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Rest配置允许上传的文件类型为",uploadFileTypes);
            }
            serviceRouterDef.getPropertyValues().addPropertyValue("uploadFileTypes", uploadFileTypes);
        }
    }

    private void setTaskExecutor(Element element, ParserContext parserContext, Object source, RootBeanDefinition serviceRouterDef) {
        if (logger.isDebugEnabled()) {
            logger.debug("Rest开始创建异步调用线程池。");
        }
        RootBeanDefinition taskExecutorDef =
                new RootBeanDefinition(org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean.class);
        String taskExecutorName = parserContext.getReaderContext().registerWithGeneratedName(taskExecutorDef);

        String corePoolSize = element.getAttribute("core-pool-size");
        if (StringUtils.hasText(corePoolSize)) {
            taskExecutorDef.getPropertyValues().addPropertyValue("corePoolSize", corePoolSize);
        } else {
            taskExecutorDef.getPropertyValues().addPropertyValue("corePoolSize", DEFAULT_CORE_POOL_SIZE);
        }

        String maxPoolSize = element.getAttribute("max-pool-size");
        if (StringUtils.hasText(maxPoolSize)) {
            taskExecutorDef.getPropertyValues().addPropertyValue("maxPoolSize", maxPoolSize);
        } else {
            taskExecutorDef.getPropertyValues().addPropertyValue("maxPoolSize", DEFAULT_MAX_POOL_SIZE);
        }

        String keepAliveSeconds = element.getAttribute("keep-alive-seconds");
        if (StringUtils.hasText(keepAliveSeconds)) {
            taskExecutorDef.getPropertyValues().addPropertyValue("keepAliveSeconds", keepAliveSeconds);
        } else {
            taskExecutorDef.getPropertyValues().addPropertyValue("keepAliveSeconds", DEFAULT_KEEP_ALIVE_SECONDS);
        }

        String queueCapacity = element.getAttribute("queue-capacity");
        if (StringUtils.hasText(queueCapacity)) {
            taskExecutorDef.getPropertyValues().addPropertyValue("queueCapacity", queueCapacity);
        } else {
            taskExecutorDef.getPropertyValues().addPropertyValue("queueCapacity", DEFAULT_QUENE_CAPACITY);
        }

        parserContext.registerComponent(new BeanComponentDefinition(taskExecutorDef, taskExecutorName));
        RuntimeBeanReference taskExecutorBeanReference = new RuntimeBeanReference(taskExecutorName);
        serviceRouterDef.getPropertyValues().add("threadPoolExecutor", taskExecutorBeanReference);
        if (logger.isDebugEnabled()) {
            logger.debug("Rest创建异步调用线程池完成。");
        }
    }

    private void setSignEnable(Element element, RootBeanDefinition serviceRouterDef) {
        String signEnable = element.getAttribute("sign-enable");
        if (StringUtils.hasText(signEnable)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Rest配置请求数据签名开关为{}",signEnable);
            }
            serviceRouterDef.getPropertyValues().addPropertyValue("signEnable", signEnable);
        }
    }

    private void setServiceTimeout(Element element, RootBeanDefinition serviceRouterDef) {
        String serviceTimeoutSeconds = element.getAttribute("service-timeout-seconds");
        if (StringUtils.hasText(serviceTimeoutSeconds)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Rest配置服务超时时间为{}秒",serviceTimeoutSeconds);
            }
            serviceRouterDef.getPropertyValues().addPropertyValue("serviceTimeoutSeconds", serviceTimeoutSeconds);
        }
    }

    private void setExtErrorBaseNames(Element element, RootBeanDefinition serviceRouterDef) {
        String extErrorBasename = element.getAttribute("ext-error-base-name");
        String extErrorBasenames = element.getAttribute("ext-error-base-names");
        if (StringUtils.hasText(extErrorBasenames)) {
            serviceRouterDef.getPropertyValues().addPropertyValue("extErrorBasenames", extErrorBasenames);
        }
        if (StringUtils.hasText(extErrorBasename)) {
            serviceRouterDef.getPropertyValues().addPropertyValue("extErrorBasename", extErrorBasename);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Rest配置国际化错误文件的基路径为{} {}",extErrorBasename,extErrorBasenames);
        }

    }

    private void setThreadFerry(Element element, RootBeanDefinition serviceRouterDef) {
        String threadFerryClassName = element.getAttribute("thread-ferry-class");
        if (StringUtils.hasText(threadFerryClassName)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Rest配置一个{},实现类为{}",ThreadFerry.class.getCanonicalName(),threadFerryClassName);
            }
            serviceRouterDef.getPropertyValues().addPropertyValue("threadFerryClassName", threadFerryClassName);
        }
    }


    /*private RuntimeBeanReference getRestValidator(Element element, Object source, ParserContext parserContext) {
        RuntimeBeanReference restValidatorRbf;
        if (element.hasAttribute("rest-validator")) {
            restValidatorRbf = new RuntimeBeanReference(element.getAttribute("rest-validator"));
            if (logger.isDebugEnabled()) {
                logger.debug("Rest装配一个自定义的RestValidator:" + restValidatorRbf.getBeanName());
            }
        } else {
            RootBeanDefinition conversionDef = new RootBeanDefinition(DefaultSecurityManager.class);
            conversionDef.setSource(source);
            conversionDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            String conversionName = parserContext.getReaderContext().registerWithGeneratedName(conversionDef);
            parserContext.registerComponent(new BeanComponentDefinition(conversionDef, conversionName));
            restValidatorRbf = new RuntimeBeanReference(conversionName);
            if (logger.isDebugEnabled()) {
                logger.debug("使用默认的RestValidator:" + DefaultSecurityManager.class.getName());
            }
        }
        return restValidatorRbf;
    }*/

}

