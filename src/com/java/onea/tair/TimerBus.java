package com.java.onea.tair;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class TimerBus {

    private static final long LIBRARY_CREATE_TIME = System.currentTimeMillis() / 1000;

    private final MethodFinder mMethodFinder;

    private final Map<Long, Set<MethodSubscriber>> subscribersByTime  = new HashMap<Long, Set<MethodSubscriber>>();

    private Timer timer = new Timer();

    private Long time = 0L;

    public TimerBus() {
        mMethodFinder = MethodFinder.INSTANCE_EVENTS;
    }

    public void register(Object object) {
        if (object == null)
            throw new NullPointerException("Object for register must be not null!");
        Map<Long, Set<MethodSubscriber>> subscribersMap = mMethodFinder.findAllEventSubscribers(object);
        for (Map.Entry<Long, Set<MethodSubscriber>> entry : subscribersMap.entrySet()) {
            Long timerKey = entry.getKey();
            Set<MethodSubscriber> availableSubscribers = subscribersByTime.get(timerKey);
            if (availableSubscribers == null) {
                availableSubscribers = new HashSet<MethodSubscriber>();
                subscribersByTime.put(timerKey, availableSubscribers);
            }
            availableSubscribers.addAll(entry.getValue());
        }
        if (subscribersMap.size() > 0) {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    TimerBus.this.time = (System.currentTimeMillis() / 1000) - LIBRARY_CREATE_TIME;
                    for (Map.Entry<Long, Set<MethodSubscriber>> entry : subscribersByTime.entrySet()) {
                        if (TimerBus.this.time % entry.getKey() == 0) {
                            for (MethodSubscriber methodSubscriber : entry.getValue())
                                invokeSubscriberMethod(methodSubscriber);
                        }
                    }
                }
            }, 0, 1000);
        }
    }

    public void unregister(Object object) {
        if (object == null)
            throw new NullPointerException("Object for register must be not null!");
        Map<Long, Set<MethodSubscriber>> subscribersMap = mMethodFinder.findAllEventSubscribers(object);
        for (Map.Entry<Long, Set<MethodSubscriber>> entry : subscribersMap.entrySet()) {
            Long timerKey = entry.getKey();
            Set<MethodSubscriber> availableSubscribers = subscribersByTime.get(timerKey);
            for (MethodSubscriber methodSubscriber : availableSubscribers) {
                if (methodSubscriber.isSubscriberFrom(object))
                    availableSubscribers.remove(methodSubscriber);
            }
        }
        if (subscribersMap.size() == 0) {
            timer.cancel();
            timer.purge();
        }
    }

    private void invokeSubscriberMethod(MethodSubscriber subscriber) {
        try {
            subscriber.handleEvent();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(String.format(
                    "Could not dispatch time: "
                            + "subscriber %s", subscriber), e);
        }
    }

    @Override
    public String toString() {
        return String.format("[TimerBus]");
    }

}
