package ru.finam.bustard;

import com.google.common.collect.Multimap;

public abstract class AbstractBustard implements Bustard {

    private final Config config;
    private final Executor defaultExecutor;
    private final Multimap<Class, Object> subscribers;

    public AbstractBustard(Executor defaultExecutor, Multimap<Class, Object> subscribersMap) {
        if (defaultExecutor == null) {
            throw new NullPointerException("defaultExecutor");
        }
        if (subscribersMap == null) {
            throw new NullPointerException("subscribersMap");
        }
        this.defaultExecutor = defaultExecutor;
        this.subscribers = subscribersMap;
        this.config = new Config();
    }

    public void attachExecutors(Executor... executors) {
        config.attachExecutors(executors);
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
