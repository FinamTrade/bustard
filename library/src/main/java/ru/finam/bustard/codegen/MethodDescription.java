package ru.finam.bustard.codegen;

public class MethodDescription {
    private final String listenerName;
    private final String methodName;
    private final String eventName;
    private String executeQualifierName;

    public MethodDescription(
            String listenerName,
            String methodName,
            String eventName,
            String executeQualifierName) {
        this.listenerName = listenerName;
        this.methodName = methodName;
        this.eventName = eventName;
        this.executeQualifierName = executeQualifierName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodDescription that = (MethodDescription) o;

        return eventName.equals(that.eventName) && listenerName.equals(that.listenerName);

    }

    @Override
    public int hashCode() {
        int result = listenerName.hashCode();
        result = 31 * result + eventName.hashCode();
        return result;
    }

    public String getListenerName() {
        return listenerName;
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
