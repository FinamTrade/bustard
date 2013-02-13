package ru.finam.bustard;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.*;

public abstract class AbstractBustard implements Bustard {

    private final Config config;
    private final Executor defaultExecutor;

    private final Multimap<Class, Object> subscribers = Multimaps.newMultimap(
            new HashMap<Class, Collection<Object>>(),
            new Supplier<Collection<Object>>() {
                @Override
                public Collection<Object> get() {
                    return new HashSet<Object>();
                }
            });


    public AbstractBustard() {
        this(new ArrayList<Executor>());
    }

    public AbstractBustard(List<Executor> executors) {
        this(new DirectExecutor(), executors);
    }

    public AbstractBustard(Executor defaultExecutor, List<Executor> executors) {
        if (defaultExecutor == null) {
            throw new NullPointerException("defaultExecutor");
        }
        this.defaultExecutor = defaultExecutor;
        this.config = new Config(executors);
    }

    protected abstract void initialize(Config config);

    protected abstract void post(Object subscriber, Object event) throws Throwable;

    @Override
    public void initialize() {
        initialize(config);
    }

    @Override
    public void subscribe(Object subscriber) {
        for (Class eventType : config.findEventTypesFor(subscriber.getClass())) {
            subscribers.put(eventType, subscriber);
        }
    }

    @Override
    public void unsubscribe(Object subscriber) {
        for (Class eventType : config.findEventTypesFor(subscriber.getClass())) {
            subscribers.remove(eventType, subscriber);
        }
    }

    protected Executor getExecutorFor(Object subscriber, Object event) {
        Executor executor = config.findExecutorFor(subscriber.getClass(), event.getClass());
        if (executor == null) {
            executor = defaultExecutor;
        }

        return executor;
    }

    @Override
    public void post(Object event) {
        for (Object subscriber : subscribers.get(event.getClass())) {
            getExecutorFor(subscriber, event).execute(new PostEvent(subscriber, event));
        }
    }

    private class PostEvent implements Runnable {

        private final Object subscriber;
        private final Object event;

        private PostEvent(Object subscriber, Object event) {
            this.subscriber = subscriber;
            this.event = event;
        }

        @Override
        public void run() {
            try {
                post(subscriber, event);
            } catch (Throwable throwable) {
                if (!(event instanceof Throwable)) {
                    post(throwable);
                }
            }
        }
    }
}
