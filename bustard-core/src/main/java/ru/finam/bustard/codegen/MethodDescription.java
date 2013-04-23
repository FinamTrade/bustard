package ru.finam.bustard.codegen;

public class MethodDescription {
    private final String listenerName;
    private final String methodName;
    private final String eventName;
    private final String eventGenericName;
    private String executeQualifierName;
    private final boolean eventOnBinding;
    private final String topic;

    public MethodDescription(
            String listenerName,
            String methodName,
            String eventName,
            String executeQualifierName,
            boolean eventOnBinding,
            String topic) {
        this.listenerName = listenerName;
        this.methodName = methodName;
        this.eventGenericName = eventName;
        this.eventName = removeGeneric(eventName);
        this.executeQualifierName = executeQualifierName;
        this.eventOnBinding = eventOnBinding;
        this.topic = topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodDescription that = (MethodDescription) o;

        return eventName.equals(that.eventName) &&
                listenerName.equals(that.listenerName) &&
                !(topic != null ? !topic.equals(that.topic) : that.topic != null);
    }

    @Override
    public int hashCode() {
        int result = listenerName.hashCode();
        result = 31 * result + eventName.hashCode();
        result = 31 * result + (topic != null ? topic.hashCode() : 0);
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

    public String getEventGenericName() {
        return eventGenericName;
    }

    private static String removeGeneric(String typeName) {
        int genericIndex = typeName.indexOf("<");
        if (genericIndex >= 0) {
            typeName = typeName.substring(0, genericIndex);
        }
        return typeName;
    }

    public String getExecuteQualifierName() {
        return executeQualifierName;
    }

    public void setExecuteQualifierName(String executeQualifierName) {
        this.executeQualifierName = executeQualifierName;
    }

    public boolean isEventOnBinding() {
        return eventOnBinding;
    }

    public String getTopic() {
        return topic;
    }
}
