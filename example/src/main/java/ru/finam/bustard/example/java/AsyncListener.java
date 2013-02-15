package ru.finam.bustard.example.java;

import net.engio.mbassy.listener.Listener;
import ru.finam.bustard.example.MessageEvent;

public class AsyncListener {

    @Async
    @Listener
    public void listen(MessageEvent messageEvent) {
        System.out.println("AsyncListener: " + messageEvent.getMessage());
    }
}
