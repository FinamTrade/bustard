package ru.finam.bustard.example;

import ru.finam.bustard.Consumes;

public class DummyListener {
    @Consumes(topic = "Dummy")
    public void listenString(String str) {
        System.out.println(str);
    }

    @Consumes
    public void handleMessage(MessageEvent event) {
        // Do nothing
    }
}
