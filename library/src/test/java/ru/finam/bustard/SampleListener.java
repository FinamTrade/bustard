package ru.finam.bustard;

public class SampleListener {

    private String lastMessage = null;

    public SampleListener() {
    }

    @SomeQualifier
    @Listener
    public void listen(String str) {
        lastMessage = str;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
