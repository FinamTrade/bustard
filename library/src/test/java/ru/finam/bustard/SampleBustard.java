package ru.finam.bustard;


import com.google.common.collect.Multimap;

/**
 * How it will be generated
 */
public class SampleBustard extends AbstractBustard {

    @Override
    void initialize(Multimap<Class<?>, Class<?>> eventTypes) {
        eventTypes.put(SampleListener.class, String.class);
    }

    @Override
    void post(Object listener, Object event) throws Throwable {
        if (event instanceof String) {
            if (listener instanceof SampleListener) {
                ((SampleListener) listener).listen((String) event);
            }
        }
    }
}
