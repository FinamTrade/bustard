package ru.finam.bustard;


public class CounterExecutor extends DirectExecutor implements Executor {
    private int count = 0;

    @Override
    public void execute(Runnable runnable) {
        super.execute(runnable);
        count++;
    }

    public int getCount() {
        return count;
    }
}
