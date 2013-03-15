package ru.finam.bustard;

public class TopicListener {
    private StringBuffer buffer = new StringBuffer();

    public TopicListener() {
    }

    @SomeQualifier
    @Consumes(topic = "SomeTopic")
    public void listen(String str) {
        buffer.append(str);
    }

    public String getBuffer() {
        return buffer.toString();
    }
}
