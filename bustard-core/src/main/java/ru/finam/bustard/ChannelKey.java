package ru.finam.bustard;

public class ChannelKey {

    private static final String SEPARATOR = "/";

    public static String get(String eventTypeName, String topic) {
        return eventTypeName + SEPARATOR + topic;
    }

    public static String get(String eventTypeName) {
        return get(eventTypeName, "");
    }

    public static String get(Class<?> eventType, String topic) {
        return get(eventType.getName(), topic);
    }

    public static String get(Class<?> eventType) {
        return get(eventType.getName());
    }

    public static String getTopic(String key) {
        int index = key.indexOf(SEPARATOR);
        return key.substring(index + 1);
    }

    public static String getTypeName(String key) {
        int index = key.indexOf(SEPARATOR);
        return key.substring(0, index);
    }


}
