package ru.finam.bustard;

/**
 * How it will be generated
 */
public class SampleBustard extends AbstractBustard {

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
