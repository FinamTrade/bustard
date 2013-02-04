package ru.finam.bustard.example;

import net.engio.mbassy.listener.Listener;

import java.io.PrintStream;
import java.io.PrintWriter;

public class MessageListener {
    private final PrintStream writer;

    public MessageListener(PrintStream writer) {
        this.writer = writer;
    }

    @Listener
    public void listen(MessageEvent event) {
        writer.println(event.getMessage());
    }
}
