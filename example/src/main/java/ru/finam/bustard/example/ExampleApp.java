package ru.finam.bustard.example;

import ru.finam.bustard.Bustard;
import ru.finam.bustard.BustardImpl;

public class ExampleApp {
    public static void main(String[] args) {
        Bustard bustard = new BustardImpl();
        bustard.initialize();

        StreamMessageListener listener = new StreamMessageListener(System.out);

        bustard.subscribe(listener);

        bustard.post(new MessageEvent("Hello World!"));
    }
}
