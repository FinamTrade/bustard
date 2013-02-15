package ru.finam.bustard.example;

import net.engio.mbassy.listener.Listener;

import java.io.PrintStream;

public class StreamMessageListener {
    private final PrintStream writer;

    public StreamMessageListener(PrintStream writer) {
        this.writer = writer;
    }

    @Listener
    public void listen(MessageEvent event) {
        writer.println("StreamMessageListener: " + event.getMessage());
    }
}
