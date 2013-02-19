package ru.finam.bustard;

import ru.finam.bustard.java.AbstractJavaBustard;

public class TestBustardWithExecutor extends AbstractJavaBustard {
    @Override
    protected void initialize(Config config) {
        config.addExecuteQualifier("SomeQualifier", CounterExecutor.class);
        config.put(BufferListener.class, String.class, "SomeQualifier", false);
    }

    @Override
    protected void post(Object subscriber, Object event) throws Throwable {
        if (event instanceof String) {
            if (subscriber instanceof BufferListener) {
                ((BufferListener) subscriber).listen((String) event);
            }
        }
    }
}
