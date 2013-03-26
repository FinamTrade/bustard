package ru.finam.bustard;

import ru.finam.bustard.java.AbstractJavaBustard;

public class TestBustardWithExecutor extends AbstractJavaBustard {
    @Override
    protected void initialize(Config config) {
        config.addExecuteQualifier("SomeQualifier", CounterExecutor.class);
        config.put(BufferListener.class, String.class.getName(), "", "SomeQualifier", false);
    }

    @Override
    protected void post(Object subscriber, Object event, String topic) throws Throwable {
        if (topic.equals(ChannelKey.get(String.class))) {
            if (subscriber instanceof BufferListener) {
                ((BufferListener) subscriber).listen((String) event);
            }
        }
    }
}
