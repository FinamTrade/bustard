package ru.finam.bustard.example.java;

import ru.finam.bustard.Listener;
import ru.finam.bustard.example.MessageEvent;

public class AsyncListener {

    @Async
    @Listener
    public void listen(MessageEvent messageEvent) {
        System.out.println("AsyncListener: " + messageEvent.getMessage());
    }
}
