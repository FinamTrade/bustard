package ru.finam.bustard;

public interface Bustard {
    void initialize();
    void attachExecutors(Executor... executors);
    <T> Channel<T> getChannelFor(ChannelKey<T> key);
    void subscribe(Object subscriber);
    void unsubscribe(Object subscriber);
    void post(Object event);
}
