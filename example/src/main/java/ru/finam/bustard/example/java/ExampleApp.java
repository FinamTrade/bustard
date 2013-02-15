package ru.finam.bustard.example.java;

import ru.finam.bustard.Bustard;
import ru.finam.bustard.BustardImpl;
import ru.finam.bustard.example.MessageEvent;
import ru.finam.bustard.example.StreamMessageListener;

public class ExampleApp {
    public static void main(String[] args) {
        Bustard bustard = new BustardImpl();
        bustard.initialize();

        StreamMessageListener listener = new StreamMessageListener(System.out);

        bustard.subscribe(listener);

        bustard.post(new MessageEvent("Hello World!"));
    }
}
