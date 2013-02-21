package ru.finam.bustard;

public interface Channel<T> {
    void post(T event);
}
