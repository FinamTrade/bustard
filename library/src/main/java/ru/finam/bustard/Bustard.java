package ru.finam.bustard;

public interface Bustard {
    void initialize();
    void subscribe(Object listener);
    void unsubscribe(Object listener);
    void post(Object event);
}
