package ru.finam.bustard.example;

import net.engio.mbassy.listener.Listener;

public class DummyListener {
    @Listener
    public void listenString(String str) {
        // Do Nothing
    }

    @Listener
    public void handleMessage(MessageEvent event) {
        // Do nothing
    }
}
