package ru.finam.bustard.gwt;

import ru.finam.bustard.AbstractBustard;

public abstract class AbstractGwtBustard extends AbstractBustard {
    public AbstractGwtBustard() {
        super(new GwtExecutor());
    }
}
