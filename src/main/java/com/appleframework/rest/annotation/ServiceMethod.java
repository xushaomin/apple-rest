/**
 *
 * 日    期：12-2-10
 */
package com.appleframework.rest.annotation;


import com.appleframework.rest.ServiceMethodDefinition;

import java.lang.annotation.*;

/**
 * <pre>
 *     使用该注解对服务方法进行标注，这些方法必须是Spring的Service:既打了@Service的注解。
 *
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceMethod {

    /**
     * 服务的方法名，即由method参数指定的服务方法名
     *
     * @return
     */
    String method() default "";

    /**
     * 所属的服务分组
     *
     * @return
     */
    String group() default ServiceMethodDefinition.DEFAULT_GROUP;

    /**
     * 所属的服务分组的标识
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
     * 服务的中文名称
     *
     * @return
     */
    String title() default "";

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
