package ru.finam.bustard;

import ru.finam.bustard.java.AbstractJavaBustard;

/**
 * How it will be generated
 */
public class TestBustard extends AbstractJavaBustard {

    @Override
    protected void initialize(Config config) {
        config.put(BufferListener.class, String.class, null, false);
        config.put(EventOnBindingListener.class, String.class, null, true);
    }

    @Override
    protected void post(Object subscriber, Object event) throws Throwable {
        if (event instanceof String) {
            if (subscriber instanceof BufferListener) {
                ((BufferListener) subscriber).listen((String) event);
            }
            if (subscriber instanceof EventOnBindingListener) {
                ((EventOnBindingListener) subscriber).listen((String) event);
            }
        }
    }
}
