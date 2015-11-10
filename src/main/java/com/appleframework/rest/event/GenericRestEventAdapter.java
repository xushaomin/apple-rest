/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-2
 */
package com.appleframework.rest.event;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.GenericTypeResolver;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class GenericRestEventAdapter implements SmartRestEventListener {

	private final RestEventListener delegate;

    public GenericRestEventAdapter(RestEventListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean supportsEventType(Class<? extends RestEvent> eventType) {
        Class<?> typeArg = GenericTypeResolver.resolveTypeArgument(this.delegate.getClass(), RestEventListener.class);
        if (typeArg == null || typeArg.equals(RestEvent.class)) {
            Class<?> targetClass = AopUtils.getTargetClass(this.delegate);
            if (targetClass != this.delegate.getClass()) {
                typeArg = GenericTypeResolver.resolveTypeArgument(targetClass, RestEventListener.class);
            }
        }
        return (typeArg == null || typeArg.isAssignableFrom(eventType));
    }

    @SuppressWarnings("unchecked")
	@Override
    public void onRestEvent(RestEvent restEvent) {
        this.delegate.onRestEvent(restEvent);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}

