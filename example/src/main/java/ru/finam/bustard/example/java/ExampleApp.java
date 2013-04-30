package ru.finam.bustard.example.java;

import ru.finam.bustard.Bustard;
import ru.finam.bustard.example.MessageEvent;
import ru.finam.bustard.example.StreamMessageListener;
import ru.finam.bustard.java.BustardFactory;

public class ExampleApp {
    public static void main(String[] args) {
        Bustard bustard = BustardFactory.createBustard();
        bustard.attachExecutors(new AsyncExecutor());
        bustard.initialize();

        AsyncListener listener = new AsyncListener();
        StreamMessageListener streamListener = new StreamMessageListener(System.out);

        bustard.subscribe(listener);
        bustard.subscribe(streamListener);

        bustard.post(new MessageEvent("Hello World!"));
    }
}
