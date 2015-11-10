/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-2
 */
package com.appleframework.rest.event;

import java.util.*;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractRestEventMulticaster implements RestEventMulticaster {

	private Set<RestEventListener> restEventListeners = new HashSet<RestEventListener>();

    private static final Map<Class<? extends RestEvent>, ListenerRegistry> cachedRestEventListeners =
            new HashMap<Class<? extends RestEvent>, ListenerRegistry>();

    @Override
    public void removeAllRestListeners() {
        restEventListeners.clear();
    }

    @Override
    public void addRestListener(RestEventListener listener) {
        restEventListeners.add(listener);
    }

    @Override
    public void removeRestListener(RestEventListener listener) {
        restEventListeners.remove(listener);
    }

    protected List<RestEventListener> getRestEventListeners(RestEvent event) {
        Class<? extends RestEvent> eventType = event.getClass();
        if (!cachedRestEventListeners.containsKey(eventType)) {
            LinkedList<RestEventListener> allListeners = new LinkedList<RestEventListener>();
            if (restEventListeners != null && restEventListeners.size() > 0) {
                for (RestEventListener restEventListener : restEventListeners) {
                    if (supportsEvent(restEventListener, eventType)) {
                        allListeners.add(restEventListener);
                    }
                }
                sortRestEventListener(allListeners);
            }
            ListenerRegistry listenerRegistry = new ListenerRegistry(allListeners);
            cachedRestEventListeners.put(eventType, listenerRegistry);
        }
        return cachedRestEventListeners.get(eventType).getRestEventListeners();
    }

    protected boolean supportsEvent(
            RestEventListener listener, Class<? extends RestEvent> eventType) {
        SmartRestEventListener smartListener = (listener instanceof SmartRestEventListener ?
                (SmartRestEventListener) listener : new GenericRestEventAdapter(listener));
        return (smartListener.supportsEventType(eventType));
    }


    protected void sortRestEventListener(List<RestEventListener> restEventListeners) {
        Collections.sort(restEventListeners, new Comparator<RestEventListener>() {
            public int compare(RestEventListener o1, RestEventListener o2) {
                if (o1.getOrder() > o2.getOrder()) {
                    return 1;
                } else if (o1.getOrder() < o2.getOrder()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }

    private class ListenerRegistry {

        public List<RestEventListener> restEventListeners;

        private ListenerRegistry(List<RestEventListener> restEventListeners) {
            this.restEventListeners = restEventListeners;
        }

        public List<RestEventListener> getRestEventListeners() {
            return restEventListeners;
        }
    }
}

