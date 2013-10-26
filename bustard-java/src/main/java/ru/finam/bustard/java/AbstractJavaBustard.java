package ru.finam.bustard.java;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import ru.finam.bustard.AbstractBustard;
import ru.finam.bustard.DirectExecutor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractJavaBustard extends AbstractBustard {

    private static Multimap<String, Object> createWeakMultiMap() {
        SetMultimap<String,Object> map = Multimaps.newSetMultimap(
                new HashMap<String, Collection<Object>>(),
                new Supplier<Set<Object>>() {
                    @Override
                    public Set<Object> get() {
                        return Collections.newSetFromMap(new WeakHashMap<Object, Boolean>());
                    }
                }
        );
        return Multimaps.synchronizedSetMultimap(map);
    }

    public AbstractJavaBustard() {
        super(new DirectExecutor(), createWeakMultiMap(), new ConcurrentHashMap<String, Object>());
    }
}
