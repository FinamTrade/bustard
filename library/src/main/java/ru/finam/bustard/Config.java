package ru.finam.bustard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

public class Config {
    private final Multimap<Class<?>, Class<?>> eventTypes = HashMultimap.create();
    private final Map<SubscriberKey, Executor> executors = new HashMap<SubscriberKey, Executor>();
    private final Map<String, Executor> executorsByQualifier = new HashMap<String, Executor>();
    private final List<Executor> executorList = new ArrayList<Executor>();

    public Config() {
    }

    public void put(Class<?> subscriberType, Class<?> eventType) {
        put(subscriberType, eventType, null);
    }

    public void attachExecutors(Executor... executors) {
        Collections.addAll(executorList, executors);
    }

    public void addExecuteQualifier(String qualifierName, Class<? extends Executor> executorType) {
        boolean added = false;
        for (Executor executor : executorList) {
            if (isAssignable(executorType, executor.getClass())) {
                if (added) {
                    throw new RuntimeException("Two executors for one qualifier: " + qualifierName);
                }
                executorsByQualifier.put(qualifierName, executor);
                added = true;
            }
        }
        if (!added) {
            throw new IllegalStateException("No executors for: " + qualifierName +
                    ". Attach executor before initialize.");
        }
    }

    private boolean isAssignable(Class<?> superType, Class<?> type) {
        while (type != superType && type != Object.class) {
            type = type.getSuperclass();
        }
        return type == superType;
    }



    public void put(Class<?> subscriberType, Class<?> eventType, String qualifierName) {
        eventTypes.put(subscriberType, eventType);
        if (qualifierName != null) {
            Executor executor = executorsByQualifier.get(qualifierName);
            executors.put(new SubscriberKey(subscriberType, eventType), executor);
        }
    }

    public Collection<Class<?>> findEventTypesFor(Class<?> subscriberType) {
        return eventTypes.get(subscriberType);
    }

    public Executor findExecutorFor(Class<?> subscriberType, Class<?> eventType) {
        return executors.get(new SubscriberKey(subscriberType, eventType));
    }

    private class SubscriberKey {
        private final Class subscriber;
        private final Class event;

        private SubscriberKey(Class<?> subscriberType, Class<?> eventType) {
            if (subscriberType == null) {
                throw new NullPointerException("subscriberType");
            }
            if (eventType == null) {
                throw new NullPointerException("eventType");
            }
            this.subscriber = subscriberType;
            this.event = eventType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SubscriberKey that = (SubscriberKey) o;

            return event == that.event && subscriber == that.subscriber;
        }

        @Override
        public int hashCode() {
            int result = subscriber.hashCode();
            result = 31 * result + event.hashCode();
            return result;
        }
    }
}
