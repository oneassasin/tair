package com.java.onea.tair;

import java.util.Map;
import java.util.Set;

public interface MethodFinder {

    // Class<?> => Subscribe event
    public Map<Long, Set<MethodSubscriber>> findAllEventSubscribers(Object listener);

    public static MethodFinder INSTANCE_EVENTS = new InstanceEventsFinder();

}
