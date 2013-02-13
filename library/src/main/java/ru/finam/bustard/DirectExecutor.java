package ru.finam.bustard;

public class DirectExecutor implements Executor {
    @Override
    public void execute(Runnable runnable) {
        runnable.run();
    }
}
