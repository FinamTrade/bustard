package ru.finam.bustard;

import ru.finam.bustard.java.AbstractJavaBustard;

/**
 * How it will be generated
 */
public class TestBustard extends AbstractJavaBustard {

    @Override
    protected void initialize(Config config) {
        config.put(BufferListener.class, String.class, "", null, false);
        config.put(EventOnBindingListener.class, String.class, "", null, true);
        config.put(TopicListener.class, String.class, "SomeTopic", null, true);
    }

    @Override
    protected void post(Object subscriber, Object event, String topic) throws Throwable {
        if (topic.equals("")) {
            if (event instanceof String) {
                if (subscriber instanceof BufferListener) {
                    ((BufferListener) subscriber).listen((String) event);
                }
                if (subscriber instanceof EventOnBindingListener) {
                    ((EventOnBindingListener) subscriber).listen((String) event);
                }
            }
        } else if (topic.equals("SomeTopic")) {
            if (event instanceof String) {
                if (subscriber instanceof TopicListener) {
                    ((TopicListener) subscriber).listen((String) event);
                }
            }
        }
    }
}
