package ru.finam.bustard;


import com.google.common.collect.Multimap;

import java.util.List;

/**
 * How it will be generated
 */
public class SampleBustard extends AbstractBustard {

    public SampleBustard(List<Executor> executors) {
        super(executors);
    }

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
