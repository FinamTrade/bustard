package ru.finam.bustard.java;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import ru.finam.bustard.AbstractBustard;
import ru.finam.bustard.DirectExecutor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.WeakHashMap;

public abstract class AbstractJavaBustard extends AbstractBustard {

    public static Multimap<Class, Object> createWeakMultiMap() {
        return Multimaps.newMultimap(new HashMap<Class, Collection<Object>>(),
                new Supplier<Collection<Object>>() {
                    @Override
                    public Collection<Object> get() {
                        return Collections.newSetFromMap(new WeakHashMap<Object, Boolean>());
                    }
                });
    }

    public AbstractJavaBustard() {
        super(new DirectExecutor(), createWeakMultiMap());
    }
}
