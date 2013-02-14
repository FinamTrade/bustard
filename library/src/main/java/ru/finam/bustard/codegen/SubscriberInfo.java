package ru.finam.bustard.codegen;

public class SubscriberInfo {
    private final String subscriberName;
    private final String methodName;
    private final String eventName;
    private final String executeQualifierName;

    public SubscriberInfo(
            String subscriberName,
            String methodName,
            String eventName,
            String executeQualifierName) {
        this.subscriberName = subscriberName;
        this.methodName = methodName;
        this.eventName = eventName;
        this.executeQualifierName = executeQualifierName;
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

    public String getExecuteQualifierName() {
        return executeQualifierName;
    }
}
