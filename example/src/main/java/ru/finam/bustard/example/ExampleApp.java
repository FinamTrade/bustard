package ru.finam.bustard.example;

import ru.finam.bustard.Bustard;
import ru.finam.bustard.BustardImpl;

import java.io.PrintWriter;

public class ExampleApp {
    public static void main(String[] args) {
        Bustard bustard = new BustardImpl();
        bustard.initialize();

        MessageListener listener = new MessageListener(System.out);

        bustard.subscribe(listener);

        bustard.post(new MessageEvent("Hello World!"));
    }
}
