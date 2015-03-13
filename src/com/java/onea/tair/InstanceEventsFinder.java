package com.java.onea.tair;

import com.java.onea.tair.annotation.ExecuteEvery;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class InstanceEventsFinder implements MethodFinder {

    @Override
    public Map<Long, Set<MethodSubscriber>> findAllEventSubscribers(Object listener) {
        Map<Long, Set<MethodSubscriber>> foundSubscribers = new HashMap<Long, Set<MethodSubscriber>>();
        Map<Method, Set<Annotation>> methods = Reflector.findAllMethodsAnnotatedWith(listener.getClass(), ExecuteEvery.class);
        for (Map.Entry<Method, Set<Annotation>> entry : methods.entrySet()) {
            checkSubscribeMethod(entry.getKey(), entry.getValue());
            if (entry.getKey().isBridge())
                continue;
            ExecuteEvery annotation = (ExecuteEvery) entry.getValue().toArray()[0];
            long timeKey = annotation.second() + annotation.minute() * 60 + annotation.hour() * 3600;
            if (timeKey == 0)
                continue;
            Set<MethodSubscriber> subscribers = foundSubscribers.get(timeKey);
            if (subscribers == null)
                subscribers = new HashSet<MethodSubscriber>();
            subscribers.add(new MethodSubscriber(listener, entry.getKey()));
            foundSubscribers.put(timeKey, subscribers);
        }
        return foundSubscribers;
    }

    private void checkSubscribeMethod(Method method, Set<Annotation> annotations) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 0) {
            throw new IllegalArgumentException("Method " + method
                    + " has @ExecuteEvery annotation but requires " + parameterTypes.length
                    + " arguments.  Methods must require a zero argument.");
        }
        if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
            throw new IllegalArgumentException("Method " + method
                    + " has @ExecuteEvery annotation but is not 'public'.");
        }
        if (!method.getReturnType().equals(Void.TYPE)) {
            throw new IllegalArgumentException("Method " + method
                    + " has @ExecuteEvery annotation but return not 'void'.");
        }
        if (annotations.size() > 1) {
            throw new IllegalArgumentException("Method " + method
                    + " has more then 1 @ExecuteEvery annotation.");
        }
    }

}

