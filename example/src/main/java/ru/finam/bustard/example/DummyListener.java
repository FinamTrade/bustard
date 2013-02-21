package ru.finam.bustard.example;

import ru.finam.bustard.Listener;

public class DummyListener {
    @Listener(topic = "Dummy")
    public void listenString(String str) {
        System.out.println(str);
    }

    @Listener
    public void handleMessage(MessageEvent event) {
        // Do nothing
    }
}
