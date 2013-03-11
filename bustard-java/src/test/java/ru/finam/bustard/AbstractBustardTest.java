package ru.finam.bustard;

import org.junit.Assert;
import org.junit.Test;

public class AbstractBustardTest {
    @Test
    public void implementingBustard() {
        Bustard bustard = new TestBustard();
        bustard.initialize();
        BufferListener listener = new BufferListener();
        bustard.subscribe(listener);
        bustard.post("Hello, World!");
        Assert.assertEquals("Hello, World!", listener.getBuffer());
    }

    @Test
    public void unsubscribeListener() {
        Bustard bustard = new TestBustard();
        bustard.initialize();
        BufferListener listener = new BufferListener();
        bustard.subscribe(listener);
        bustard.post("Hi");
        bustard.unsubscribe(listener);
        bustard.post("Bye");
        Assert.assertEquals("Hi", listener.getBuffer());
    }

    @Test
    public void attachExecutorToBustard() {
        Bustard bustard = new TestBustardWithExecutor();
        CounterExecutor counterExecutor = new CounterExecutor();
        bustard.attachExecutors(counterExecutor);
        bustard.initialize();
        BufferListener listener = new BufferListener();
        bustard.subscribe(listener);
        bustard.post("Hello, World!");
        Assert.assertEquals("Hello, World!", listener.getBuffer());
        Assert.assertEquals(1, counterExecutor.getCount());
    }

    @Test(expected = IllegalStateException.class)
    public void noExecutor() {
        Bustard bustard = new TestBustardWithExecutor();
        bustard.initialize();
    }

    @Test
    public void eventOnBinding() {
        Bustard bustard = new TestBustard();
        bustard.initialize();
        bustard.post("Hello");
        BufferListener listener = new BufferListener();
        EventOnBindingListener eventOnBindingListener = new EventOnBindingListener();
        bustard.subscribe(listener);
        bustard.subscribe(eventOnBindingListener);
        Assert.assertEquals("Hello", eventOnBindingListener.getBuffer());
        Assert.assertEquals("", listener.getBuffer());
    }

    @Test
    public void eventTopic() {
        Bustard bustard = new TestBustard();
        bustard.initialize();
        BufferListener listener = new BufferListener();
        TopicListener topicListener = new TopicListener();
        bustard.subscribe(listener);
        bustard.subscribe(topicListener);
        bustard.post("Foo");
        bustard.getChannelFor(ChannelKey.get(String.class, "SomeTopic")).post("Bar");
        Assert.assertEquals("Foo", listener.getBuffer());
        Assert.assertEquals("Bar", topicListener.getBuffer());
    }
}
