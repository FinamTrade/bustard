package ru.finam.bustard;

public class SampleBustardWithExecutor extends AbstractBustard {
    @Override
    protected void initialize(Config config) {
        config.addExecuteQualifier("SomeQualifier", CounterExecutor.class);
        config.put(SampleListener.class, String.class, "SomeQualifier");
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
