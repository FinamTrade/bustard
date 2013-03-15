package ru.finam.bustard.example.java;

import ru.finam.bustard.Consumes;
import ru.finam.bustard.example.MessageEvent;

public class AsyncListener {

    @Async
    @Consumes
    public void listen(MessageEvent messageEvent) {
        System.out.println("AsyncListener: " + messageEvent.getMessage());
    }
}
