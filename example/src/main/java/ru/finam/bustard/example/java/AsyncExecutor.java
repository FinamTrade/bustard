package ru.finam.bustard.example.java;

import ru.finam.bustard.Executor;

public class AsyncExecutor implements Executor {

    @Override
    public void execute(Runnable runnable) {
        new Thread(runnable).start();
    }
}
