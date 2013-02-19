package ru.finam.bustard;

import junit.framework.Assert;
import org.junit.Test;

public class AbstractBustardTest {
    @Test
    public void implementingBustard() {
        Bustard bustard = new SampleBustard();
        bustard.initialize();
        BufferListener listener = new BufferListener();
        bustard.subscribe(listener);
        bustard.post("Hello, World!");
        Assert.assertEquals("Hello, World!", listener.getBuffer());
    }

    @Test
    public void unsubscribeListener() {
        Bustard bustard = new SampleBustard();
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
        Bustard bustard = new SampleBustardWithExecutor();
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
        Bustard bustard = new SampleBustardWithExecutor();
        bustard.initialize();
    }

    @Test
    public void eventOnBinding() {
        Bustard bustard = new SampleBustard();
        bustard.initialize();
        bustard.post("Hello");
        BufferListener listener = new BufferListener();
        EventOnBindingListener eventOnBindingListener = new EventOnBindingListener();
        bustard.subscribe(listener);
        bustard.subscribe(eventOnBindingListener);
        Assert.assertEquals("Hello", eventOnBindingListener.getBuffer());
        Assert.assertEquals("", listener.getBuffer());
    }
}
