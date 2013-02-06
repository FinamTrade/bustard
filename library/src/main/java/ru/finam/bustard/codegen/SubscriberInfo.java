package ru.finam.bustard.codegen;

public class SubscriberInfo {
    private final String subscriberName;
    private final String methodName;
    private final String eventName;

    public SubscriberInfo(String subscriberName, String methodName, String eventName) {
        this.subscriberName = subscriberName;
        this.methodName = methodName;
        this.eventName = eventName;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getEventName() {
        return eventName;
    }
}
