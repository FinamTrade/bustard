package ru.finam.bustard;

import ru.finam.bustard.java.AbstractJavaBustard;

/**
 * How it will be generated
 */
public class TestBustard extends AbstractJavaBustard {

    @Override
    protected void initialize(Config config) {
        config.put(BufferListener.class, String.class.getName(), "", null, false);
        config.put(EventOnBindingListener.class, String.class.getName(), "", null, true);
        config.put(TopicListener.class, String.class.getName(), "SomeTopic", null, true);
    }

    @Override
    protected void post(Object subscriber, Object event, String key) throws Throwable {
        if (key.equals(ChannelKey.get(String.class))) {
            if (subscriber instanceof BufferListener) {
                ((BufferListener) subscriber).listen((String) event);
            }
            if (subscriber instanceof EventOnBindingListener) {
                ((EventOnBindingListener) subscriber).listen((String) event);
            }
        } else if (key.equals(ChannelKey.get(String.class, "SomeTopic"))) {
            if (subscriber instanceof TopicListener) {
                ((TopicListener) subscriber).listen((String) event);
            }
        }
    }
}
