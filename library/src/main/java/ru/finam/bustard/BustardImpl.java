package ru.finam.bustard;

import com.google.common.collect.Multimap;

import java.util.List;

/**
 * Stub
 */
public class BustardImpl extends AbstractBustard {
    public BustardImpl(List<Executor> executors) {
        super(executors);
    }

    @Override
    protected void initialize(Config config) {
        throw new UnsupportedOperationException("Stub bustard using.");
    }

    @Override
    protected void post(Object subscriber, Object event) throws Throwable {
        throw new UnsupportedOperationException("Stub bustard using.");
    }
}
