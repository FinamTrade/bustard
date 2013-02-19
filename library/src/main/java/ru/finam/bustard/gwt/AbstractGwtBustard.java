package ru.finam.bustard.gwt;

import com.google.common.collect.HashMultimap;
import ru.finam.bustard.AbstractBustard;

import java.util.HashMap;

public abstract class AbstractGwtBustard extends AbstractBustard {
    public AbstractGwtBustard() {
        super(new GwtExecutor(), HashMultimap.<Class, Object>create(), new HashMap<Class, Object>());
    }
}
