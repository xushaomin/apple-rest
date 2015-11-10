/**
 *
 * 日    期：12-2-11
 */
package com.appleframework.rest.impl;

import com.appleframework.rest.*;
import com.appleframework.rest.annotation.*;
import com.appleframework.rest.request.UploadFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * <pre>
 *    REST框架的上下文
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class DefaultRestContext implements RestContext {

    protected static Logger logger = LoggerFactory.getLogger(DefaultRestContext.class);

    private final Map<String, ServiceMethodHandler> serviceHandlerMap = new HashMap<String, ServiceMethodHandler>();

    private final Set<String> serviceMethods = new HashSet<String>();

    public DefaultRestContext(ApplicationContext context) {
        registerFromContext(context);
    }

    public void addServiceMethod(String methodName, ServiceMethodHandler serviceMethodHandler) {
        serviceMethods.add(methodName);
        serviceHandlerMap.put(ServiceMethodHandler.methodWithVersion(methodName), serviceMethodHandler);
    }


    public ServiceMethodHandler getServiceMethodHandler(String methodName) {
        return serviceHandlerMap.get(ServiceMethodHandler.methodWithVersion(methodName));
    }

    public boolean isValidMethod(String methodName) {
        return serviceMethods.contains(methodName);
    }


    public boolean isValidVersion(String methodName) {
        return serviceHandlerMap.containsKey(ServiceMethodHandler.methodWithVersion(methodName));
    }


    public boolean isVersionObsoleted(String methodName, String version) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public Map<String, ServiceMethodHandler> getAllServiceMethodHandlers() {
        return serviceHandlerMap;
    }

    /**
     * 扫描Spring容器中的Bean，查找有标注{@link ServiceMethod}注解的服务方法，将它们注册到{@link RestContext}中缓存起来。
     *
     * @throws org.springframework.beans.BeansException
     *
     */
    private void registerFromContext(final ApplicationContext context) throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("对Spring上下文中的Bean进行扫描，查找REST服务方法: " + context);
        }
        String[] beanNames = context.getBeanNamesForType(Object.class);
        for (final String beanName : beanNames) {
            Class<?> handlerType = context.getType(beanName);
            //只对标注 ServiceMethodBean的Bean进行扫描
            if(AnnotationUtils.findAnnotation(handlerType,ServiceMethodBean.class) != null){
                ReflectionUtils.doWithMethods(handlerType, new ReflectionUtils.MethodCallback() {
                            @SuppressWarnings("unchecked")
							public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                                ReflectionUtils.makeAccessible(method);

                                ServiceMethod serviceMethod = AnnotationUtils.findAnnotation(method,ServiceMethod.class);
                                ServiceMethodBean serviceMethodBean =AnnotationUtils.findAnnotation(method.getDeclaringClass(),ServiceMethodBean.class);

                                ServiceMethodDefinition definition = null;
                                if (serviceMethodBean != null) {
                                    definition = buildServiceMethodDefinition(serviceMethodBean, serviceMethod);
                                } else {
                                    definition = buildServiceMethodDefinition(serviceMethod);
                                }
                                ServiceMethodHandler serviceMethodHandler = new ServiceMethodHandler();
                                serviceMethodHandler.setServiceMethodDefinition(definition);

                                //1.set handler
                                serviceMethodHandler.setHandler(context.getBean(beanName)); //handler
                                serviceMethodHandler.setHandlerMethod(method); //handler'method


                                if (method.getParameterTypes().length > 1) {//handler method's parameter
                                    throw new RestException(method.getDeclaringClass().getName() + "." + method.getName()
                                            + "的入参只能是" + RestRequest.class.getName() + "或无入参。");
                                } else if (method.getParameterTypes().length == 1) {
                                    Class<?> paramType = method.getParameterTypes()[0];
                                    if (!ClassUtils.isAssignable(RestRequest.class, paramType)) {
                                        throw new RestException(method.getDeclaringClass().getName() + "." + method.getName()
                                                + "的入参必须是" + RestRequest.class.getName());
                                    }
                                    boolean restRequestImplType = !(paramType.isAssignableFrom(RestRequest.class) ||
                                            paramType.isAssignableFrom(AbstractRestRequest.class));
                                    serviceMethodHandler.setRestRequestImplType(restRequestImplType);
                                    serviceMethodHandler.setRequestType((Class<? extends RestRequest>) paramType);
                                } else {
                                    logger.info(method.getDeclaringClass().getName() + "." + method.getName() + "无入参");
                                }

                                //3.set fileItemFieldNames
                                serviceMethodHandler.setUploadFileFieldNames(getFileItemFieldNames(serviceMethodHandler.getRequestType()));

                                addServiceMethod(definition.getMethod(), serviceMethodHandler);

                                if (logger.isDebugEnabled()) {
                                    logger.debug("注册服务方法：" + method.getDeclaringClass().getCanonicalName() +
                                            "#" + method.getName() + "(..)");
                                }
                            }
                        },
                        new ReflectionUtils.MethodFilter() {
                            public boolean matches(Method method) {
                                return !method.isSynthetic() && AnnotationUtils.findAnnotation(method, ServiceMethod.class) != null;
                            }
                        }
                );
            }
        }
        if (context.getParent() != null) {
            registerFromContext(context.getParent());
        }
        if (logger.isInfoEnabled()) {
            logger.info("共注册了" + serviceHandlerMap.size() + "个服务方法");
        }
    }

    private ServiceMethodDefinition buildServiceMethodDefinition(ServiceMethod serviceMethod) {
        ServiceMethodDefinition definition = new ServiceMethodDefinition();
        definition.setMethod(serviceMethod.method());
        definition.setMethodTitle(serviceMethod.title());
        definition.setMethodGroup(serviceMethod.group());
        definition.setMethodGroupTitle(serviceMethod.groupTitle());
        definition.setTags(serviceMethod.tags());
        definition.setTimeout(serviceMethod.timeout());
        definition.setHttpAction(serviceMethod.httpAction());
        return definition;
    }

    private ServiceMethodDefinition buildServiceMethodDefinition(ServiceMethodBean serviceMethodBean, ServiceMethod serviceMethod) {
        ServiceMethodDefinition definition = new ServiceMethodDefinition();
        definition.setMethodGroup(serviceMethodBean.group());
        definition.setMethodGroupTitle(serviceMethodBean.groupTitle());
        definition.setTags(serviceMethodBean.tags());
        definition.setTimeout(serviceMethodBean.timeout());
        definition.setHttpAction(serviceMethodBean.httpAction());

        //如果ServiceMethod所提供的值和ServiceMethodGroup不一样，覆盖之
        definition.setMethod(serviceMethod.method());
        definition.setMethodTitle(serviceMethod.title());

        if (!ServiceMethodDefinition.DEFAULT_GROUP.equals(serviceMethod.group())) {
            definition.setMethodGroup(serviceMethod.group());
        }

        if (!ServiceMethodDefinition.DEFAULT_GROUP_TITLE.equals(serviceMethod.groupTitle())) {
            definition.setMethodGroupTitle(serviceMethod.groupTitle());
        }

        if (serviceMethod.tags() != null && serviceMethod.tags().length > 0) {
            definition.setTags(serviceMethod.tags());
        }

        if (serviceMethod.timeout() > 0) {
            definition.setTimeout(serviceMethod.timeout());
        }

        if (serviceMethod.httpAction().length > 0) {
            definition.setHttpAction(serviceMethod.httpAction());
        }

        return definition;
    }

    private List<String> getFileItemFieldNames(Class<? extends RestRequest> requestType) {
        final ArrayList<String> fileItemFieldNames = new ArrayList<String>(1);
        if (requestType != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("获取" + requestType.getCanonicalName() + "类型为FileItem的字段名");
            }

            ReflectionUtils.doWithFields(requestType, new ReflectionUtils.FieldCallback() {
                        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                            fileItemFieldNames.add(field.getName());
                        }
                    },
                    new ReflectionUtils.FieldFilter() {
                        public boolean matches(Field field) {
                            return ClassUtils.isAssignable(UploadFile.class, field.getType());
                        }
                    }
            );

        }
        return fileItemFieldNames;
    }


}

