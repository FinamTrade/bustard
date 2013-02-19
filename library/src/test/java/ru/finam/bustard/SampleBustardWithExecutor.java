package ru.finam.bustard;

import ru.finam.bustard.java.AbstractJavaBustard;

public class SampleBustardWithExecutor extends AbstractJavaBustard {
    @Override
    protected void initialize(Config config) {
        config.addExecuteQualifier("SomeQualifier", CounterExecutor.class);
        config.put(SampleListener.class, String.class, "SomeQualifier", false);
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
