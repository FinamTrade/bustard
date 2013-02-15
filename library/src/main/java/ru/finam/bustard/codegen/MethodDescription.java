package ru.finam.bustard.codegen;

public class MethodDescription {
    private final String subscriberName;
    private final String methodName;
    private final String eventName;
    private String executeQualifierName;

    public MethodDescription(
            String subscriberName,
            String methodName,
            String eventName,
            String executeQualifierName) {
        this.subscriberName = subscriberName;
        this.methodName = methodName;
        this.eventName = eventName;
        this.executeQualifierName = executeQualifierName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodDescription that = (MethodDescription) o;

        return eventName.equals(that.eventName) && subscriberName.equals(that.subscriberName);

    }

    @Override
    public int hashCode() {
        int result = subscriberName.hashCode();
        result = 31 * result + eventName.hashCode();
        return result;
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

    public void setExecuteQualifierName(String executeQualifierName) {
        this.executeQualifierName = executeQualifierName;
    }
}
