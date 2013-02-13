package ru.finam.bustard;

public interface Bustard {
    void initialize();
    void subscribe(Object subscriber);
    void unsubscribe(Object subscriber);
    void post(Object event);
}
