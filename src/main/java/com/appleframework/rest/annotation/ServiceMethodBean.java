/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-5-31
 */
package com.appleframework.rest.annotation;

import com.appleframework.rest.ServiceMethodDefinition;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * <pre>
 *    在服务类中标该类，以便确定服务方法所属的组及相关信息。由于ApiMethodGroup已经标注了
 * Spring的{@link Component}注解，因此标注了{@link ServiceMethodBean}的类自动成为Spring的Bean.
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface ServiceMethodBean {

    String value() default "";
    /**
     * 所属的服务分组，默认为"DEFAULT"
     *
     * @return
     */
    String group() default ServiceMethodDefinition.DEFAULT_GROUP;

    /**
     * 组中文名
     *
     * @return
     */
    String groupTitle() default ServiceMethodDefinition.DEFAULT_GROUP_TITLE;

    /**
     * 标签，可以打上多个标签
     *
     * @return
     */
    String[] tags() default {};

    /**
     * 访问过期时间，单位为毫秒，即大于这个过期时间的链接会结束并返回错误报文，如果
     * 为0或负数则表示不进行过期限制
     *
     * @return
     */
    int timeout() default -1;

    /**
     * 请求方法，默认不限制
     *
     * @return
     */
    HttpAction[] httpAction() default {};

    /**
     * 服务方法是否已经过期，默认不过期
     * @return
     */
    ObsoletedType obsoleted() default  ObsoletedType.DEFAULT;
}

