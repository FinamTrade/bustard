package ru.finam.bustard.gwt;

import ru.finam.bustard.AbstractBustard;
import ru.finam.bustard.Executor;

import java.util.List;

public abstract class AbstractGwtBustard extends AbstractBustard {
    public AbstractGwtBustard(List<Executor> executors) {
        super(new GwtExecutor(), executors);
    }
}
