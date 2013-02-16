package ru.finam.bustard;

import ru.finam.bustard.java.AbstractJavaBustard;

/**
 * How it will be generated
 */
public class SampleBustard extends AbstractJavaBustard {

    @Override
    protected void initialize(Config config) {
        config.put(SampleListener.class, String.class);
    }

    @Override
    protected void post(Object subscriber, Object event) throws Throwable {
        if (event instanceof String) {
            if (subscriber instanceof SampleListener) {
                ((SampleListener) subscriber).listen((String) event);
            }
        }
    }
}
