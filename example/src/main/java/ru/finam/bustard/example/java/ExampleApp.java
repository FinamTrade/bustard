package ru.finam.bustard.example.java;

import ru.finam.bustard.Bustard;
import ru.finam.bustard.java.BustardImpl;
import ru.finam.bustard.example.MessageEvent;
import ru.finam.bustard.example.StreamMessageListener;

public class ExampleApp {
    public static void main(String[] args) {
        Bustard bustard = new BustardImpl();
        bustard.attachExecutors(new AsyncExecutor());
        bustard.initialize();

        AsyncListener listener = new AsyncListener();
        StreamMessageListener streamListener = new StreamMessageListener(System.out);

        bustard.subscribe(listener);
        bustard.subscribe(streamListener);

        bustard.post(new MessageEvent("Hello World!"));
    }
}
