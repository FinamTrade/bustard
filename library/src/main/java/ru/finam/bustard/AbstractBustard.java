package ru.finam.bustard;

import com.google.common.base.Supplier;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public abstract class AbstractBustard implements Bustard {

    private final Multimap<Class, Class> eventTypes = HashMultimap.create();

    private final Multimap<Class, Object> listeners = Multimaps.newMultimap(
            new HashMap<Class, Collection<Object>>(),
            new Supplier<Collection<Object>>() {
                @Override
                public Collection<Object> get() {
                    return new HashSet<Object>();
                }
            });

    abstract void initialize(Multimap<Class, Class> eventTypes);

    abstract void post(Object listener, Object event) throws Throwable;

    @Override
    public void initialize() {
        initialize(eventTypes);
    }

    @Override
    public void subscribe(Object listener) {
        print("subscribe: " + listener.getClass());
        for (Class eventType : eventTypes.get(listener.getClass())) {
            print(eventType.toString());
            listeners.put(eventType, listener);
        }
    }

    @Override
    public void unsubscribe(Object listener) {
        for (Class eventType : eventTypes.get(listener.getClass())) {
            listeners.remove(eventType, listener);
        }
    }

    protected void print(String message) {
        log(message);
    }

    private native void log(String message)/*-{
        console.log(message);
    }-*/;

    @Override
    public void post(Object event) {
        print("Calling post(" + event.getClass() + ")");
        print(listeners.toString());
        for (Object listener : listeners.get(event.getClass())) {
            try {
                print("Calling post(Object, Object)");
                post(listener, event);
            } catch (Throwable throwable) {
                if (!(event instanceof Throwable)) {
                    post(throwable);
                }
            }
        }
    }
}
