package ru.finam.bustard;

import junit.framework.Assert;
import org.junit.Test;

public class AbstractBustardTest {
    @Test
    public void implementingBustard() {
        Bustard bustard = new SampleBustard();
        bustard.initialize();
        SampleListener listener = new SampleListener();
        bustard.subscribe(listener);
        bustard.post("Hello, World!");
        Assert.assertEquals("Hello, World!", listener.getLastMessage());
    }

    @Test
    public void unsubscribeListener() {
        Bustard bustard = new SampleBustard();
        bustard.initialize();
        SampleListener listener = new SampleListener();
        bustard.subscribe(listener);
        bustard.post("Hi");
        bustard.unsubscribe(listener);
        bustard.post("Bye");
        Assert.assertEquals("Hi", listener.getLastMessage());
    }

    @Test
    public void attachExecutorToBustard() {
        Bustard bustard = new SampleBustardWithExecutor();
        CounterExecutor counterExecutor = new CounterExecutor();
        bustard.attachExecutors(counterExecutor);
        bustard.initialize();
        SampleListener listener = new SampleListener();
        bustard.subscribe(listener);
        bustard.post("Hello, World!");
        Assert.assertEquals("Hello, World!", listener.getLastMessage());
        Assert.assertEquals(1, counterExecutor.getCount());
    }
}
