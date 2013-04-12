package ru.finam.bustard.test;

import dagger.ObjectGraph;
import ru.finam.bustard.Bustard;
import ru.finam.bustard.BustardEntryPoint;
import ru.finam.bustard.Channel;
import ru.finam.bustard.Topic;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class ChannelHolder extends BustardEntryPoint {
    @Inject
    @Topic("SomeTopic")
    Channel<List<String>> channel1;

    @Inject
    Channel<List<String>> channel2;

    @Inject
    Bustard bustard;

    private GenericListener listener = new GenericListener();
    private GenericListenerWithTopic listenerWithTopic = new GenericListenerWithTopic();

    public ChannelHolder() {
    }

    @Override
    protected void onLoad() {
        ObjectGraph objectGraph = ObjectGraph.create(new ExampleModule());
        ChannelHolder channelHolder = objectGraph.get(ChannelHolder.class);
        channelHolder.initialize();
        channelHolder.channel1.post(Arrays.asList("Foo", "Bar"));
        channelHolder.channel2.post(Arrays.asList("Abc", "Efg"));

        send(channelHolder.listener.strings.contains("Abc"));
        send(channelHolder.listener.strings.contains("Efg"));
        send(channelHolder.listenerWithTopic.strings.contains("Foo"));
        send(channelHolder.listenerWithTopic.strings.contains("Bar"));

        send(!channelHolder.listenerWithTopic.strings.contains("Abc"));
        send(!channelHolder.listener.strings.contains("Foo"));
    }

    public native void send(boolean result) /*-{
        console.log(result ? "Ok." : "Failed.");
    }-*/;

    public void initialize() {
        bustard.subscribe(listener);
        bustard.subscribe(listenerWithTopic);
    }
}
