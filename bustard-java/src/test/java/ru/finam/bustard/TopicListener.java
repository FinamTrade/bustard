package ru.finam.bustard;

public class TopicListener {
    private StringBuffer buffer = new StringBuffer();

    public TopicListener() {
    }

    @SomeQualifier
    @Listener(topic = "SomeTopic")
    public void listen(String str) {
        buffer.append(str);
    }

    public String getBuffer() {
        return buffer.toString();
    }
}
