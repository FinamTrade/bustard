package ru.finam.bustard;

/**
 * Created with IntelliJ IDEA.
 * User: ylevin
 * Date: 19.02.13
 * Time: 15:16
 */
public class EventOnBindingListener {
    private String lastMessage = null;

    public EventOnBindingListener() {
    }

    @SomeQualifier
    @Listener(eventOnBinding = true)
    public void listen(String str) {
        lastMessage = str;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
