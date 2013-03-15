package ru.finam.bustard;

/**
 * Created with IntelliJ IDEA.
 * User: ylevin
 * Date: 19.02.13
 * Time: 15:16
 */
public class EventOnBindingListener {
    private StringBuffer buffer = new StringBuffer();

    public EventOnBindingListener() {
    }

    @SomeQualifier
    @Consumes(eventOnBinding = true)
    public void listen(String str) {
        buffer.append(str);
    }

    public String getBuffer() {
        return buffer.toString();
    }
}
