package ru.finam.bustard.test;

import com.google.gwt.user.client.Timer;
import dagger.DaggerEntryPoint;
import ru.finam.bustard.Bustard;
import ru.finam.bustard.Channel;
import ru.finam.bustard.Topic;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class ChannelHolder extends DaggerEntryPoint {
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
        initialize();
        channel1.post(Arrays.asList("Foo", "Bar"));
        channel2.post(Arrays.asList("Abc", "Efg"));

        new Timer() {
            @Override
            public void run() {
                check(listener.strings.contains("Abc"), "listener contains \"Abc\"");
                check(listener.strings.contains("Efg"), "listener contains \"Efg\"");
                check(listenerWithTopic.strings.contains("Foo"), "listenerWithTopic contains \"Foo\"");
                check(listenerWithTopic.strings.contains("Bar"));

                check(!listenerWithTopic.strings.contains("Abc"), "listenerWithTopic not contains \"Abc\"");
                check(!listener.strings.contains("Foo"));
            }
        }.schedule(10);
    }

    public void check(boolean result) {
        check(result, null);
    }

    public native void check(boolean result, String message) /*-{
        console.log((message == null ? "" : (message + ": ")) + (result ? "Ok." : "Failed."));
    }-*/;

    public void initialize() {
        bustard.subscribe(listener);
        bustard.subscribe(listenerWithTopic);
    }
}
