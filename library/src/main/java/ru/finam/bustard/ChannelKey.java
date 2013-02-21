package ru.finam.bustard;

public class ChannelKey<T> {
    private final String topic;
    private final Class<T> eventType;

    public ChannelKey(Class<T> eventType, String topic) {
        if (eventType == null) {
            throw new NullPointerException("eventType");
        }
        if (topic == null) {
            throw new NullPointerException("topic");
        }
        this.topic = topic;
        this.eventType = eventType;
    }

    public ChannelKey(Class<T> eventType) {
        this(eventType, "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChannelKey that = (ChannelKey) o;

        return eventType == that.eventType &&
                topic.equals(that.topic);

    }

    @Override
    public int hashCode() {
        int result = topic.hashCode();
        result = 31 * result + eventType.hashCode();
        return result;
    }

    public String getTopic() {
        return topic;
    }

    public Class<T> getEventType() {
        return eventType;
    }
}
