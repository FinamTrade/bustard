package ru.finam.bustard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

public class Config {
    private final Multimap<Class<?>, ChannelKey<?>> eventTypes = HashMultimap.create();
    private final Multimap<ChannelKey<?>, Class<?>> eventsOnBinding = HashMultimap.create();
    private final Map<SubscriberKey, Executor> executors = new HashMap<SubscriberKey, Executor>();
    private final Map<String, Executor> executorsByQualifier = new HashMap<String, Executor>();
    private final List<Executor> executorList = new ArrayList<Executor>();

    public Config() {
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

    @SuppressWarnings("unchecked")
    private ChannelKey<?> key(Class<?> eventType, String topic) {
        return new ChannelKey(eventType, topic);
    }

    public void put(Class<?> listenerType, Class<?> eventType,
                    String topic, String qualifierName, boolean eventOnBinding) {
        ChannelKey key = key(eventType, topic);

        eventTypes.put(listenerType, key);
        if (qualifierName != null) {
            Executor executor = executorsByQualifier.get(qualifierName);
            executors.put(new SubscriberKey(listenerType, eventType, topic), executor);
        }
        if (eventOnBinding) {
            eventsOnBinding.put(key, listenerType);
        }
    }

    public Collection<ChannelKey<?>> findEventTypesFor(Class<?> subscriberType) {
        return eventTypes.get(subscriberType);
    }

    public Executor findExecutorFor(Class<?> subscriberType, ChannelKey<?> eventType) {
        return executors.get(new SubscriberKey(subscriberType, eventType.getEventType(), eventType.getTopic()));
    }

    public boolean isEventOnBinding(Class<?> subscriberType, ChannelKey<?> key) {
        return eventsOnBinding.get(key) != null &&
                eventsOnBinding.get(key).contains(subscriberType);
    }

    public boolean needToSave(ChannelKey<?> eventType) {
        return eventsOnBinding.containsKey(eventType);
    }

    private class SubscriberKey {
        private final Class subscriber;
        private final Class event;
        private final String topic;

        private SubscriberKey(Class<?> subscriberType, Class<?> eventType, String topic) {
            if (subscriberType == null) {
                throw new NullPointerException("subscriberType");
            }
            if (eventType == null) {
                throw new NullPointerException("eventType");
            }
            if (topic == null) {
                throw new NullPointerException("topic");
            }
            this.subscriber = subscriberType;
            this.event = eventType;
            this.topic = topic;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SubscriberKey that = (SubscriberKey) o;

            return event == that.event &&
                    subscriber == that.subscriber &&
                    topic.equals(that.topic);

        }

        @Override
        public int hashCode() {
            int result = subscriber.hashCode();
            result = 31 * result + event.hashCode();
            result = 31 * result + topic.hashCode();
            return result;
        }
    }
}
