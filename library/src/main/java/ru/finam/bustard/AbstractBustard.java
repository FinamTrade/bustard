package ru.finam.bustard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public abstract class AbstractBustard implements Bustard {

    private final Multimap<Class<?>, Class<?>> eventTypes = HashMultimap.create();

    private final Multimap<Class<?>, Object> listeners = HashMultimap.create();

    abstract void initialize(Multimap<Class<?>, Class<?>> eventTypes);

    abstract void post(Object listener, Object event) throws Throwable;

    @Override
    public void initialize() {
        initialize(eventTypes);
    }

    @Override
    public void subscribe(Object listener) {
        for (Class eventType : eventTypes.get(listener.getClass())) {
            listeners.put(eventType, listener);
        }
    }

    @Override
    public void unsubscribe(Object listener) {
        for (Class eventType : eventTypes.get(listener.getClass())) {
            listeners.remove(eventType, listener);
        }
    }

    @Override
    public void post(Object event) {
        for (Object listener : listeners.get(event.getClass())) {
            try {
                post(listener, event);
            } catch (Throwable throwable) {
                if (!(event instanceof Throwable)) {
                    post(throwable);
                }
            }
        }
    }
}
