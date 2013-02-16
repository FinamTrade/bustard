package ru.finam.bustard.gwt;

import com.google.common.collect.HashMultimap;
import ru.finam.bustard.AbstractBustard;

public abstract class AbstractGwtBustard extends AbstractBustard {
    public AbstractGwtBustard() {
        super(new GwtExecutor(), HashMultimap.<Class, Object>create());
    }
}
