package ru.finam.bustard;

public class BufferListener {

    private StringBuffer buffer = new StringBuffer();

    public BufferListener() {
    }

    @SomeQualifier
    @Listener
    public void listen(String str) {
        buffer.append(str);
    }

    public String getBuffer() {
        return buffer.toString();
    }
}
