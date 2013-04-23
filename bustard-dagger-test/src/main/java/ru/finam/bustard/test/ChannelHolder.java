package ru.finam.bustard.test;

import dagger.ObjectGraph;
import ru.finam.bustard.Bustard;
import ru.finam.bustard.Channel;
import ru.finam.bustard.Topic;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class ChannelHolder {
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

    public void initialize() {
        bustard.subscribe(listener);
        bustard.subscribe(listenerWithTopic);
    }

    public static void main(String[] args) {
        ObjectGraph objectGraph = ObjectGraph.create(new ExampleModule());
        ChannelHolder channelHolder = objectGraph.get(ChannelHolder.class);
        channelHolder.initialize();
        channelHolder.channel1.post(Arrays.asList("Foo", "Bar"));
        channelHolder.channel2.post(Arrays.asList("Abc", "Efg"));

        assert channelHolder.listener.strings.contains("Abc");
        assert channelHolder.listener.strings.contains("Efg");
        assert channelHolder.listenerWithTopic.strings.contains("Foo");
        assert channelHolder.listenerWithTopic.strings.contains("Bar");

        assert !channelHolder.listenerWithTopic.strings.contains("Abc");
        assert !channelHolder.listener.strings.contains("Foo");
    }
}
