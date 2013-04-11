package ru.finam.bustard;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;

import java.util.Map;

public abstract class AbstractBustard implements Bustard {

    private final Config config;
    private final Executor defaultExecutor;
    private final Multimap<String, Object> subscribers;
    private final Map<String, Object> savedEvents;

    public AbstractBustard(Executor defaultExecutor,
                           Multimap<String, Object> subscribersMap,
                           Map<String, Object> eventsMap) {
        Preconditions.checkNotNull(defaultExecutor, "defaultExecutor");
        Preconditions.checkNotNull(subscribersMap, "subscribersMap");
        Preconditions.checkNotNull(eventsMap, "eventsMap");

        this.defaultExecutor = defaultExecutor;
        this.subscribers = subscribersMap;
        this.savedEvents = eventsMap;
        this.config = new Config();
    }

    @Override
    public void attachExecutors(Executor... executors) {
        config.attachExecutors(executors);
    }

    @Override
    public <T> Channel<T> getChannelFor(final String key) {
        return new Channel<T>() {
            @Override
            public void post(T event) {
                AbstractBustard.this.post(key, event);
            }
        };
    }

    protected abstract void initialize(Config config);

    protected abstract void post(Object subscriber, Object event, String key) throws Throwable;

    @Override
    public void initialize() {
        initialize(config);
    }

    @Override
    public void subscribe(Object subscriber) {
        for (String key : config.findEventTypesFor(subscriber.getClass())) {
            if (config.isEventOnBinding(subscriber.getClass(), key) &&
                    savedEvents.containsKey(key)) {
                postToSubscriber(subscriber, key, savedEvents.get(key));
            }
            subscribers.put(key, subscriber);
        }
    }

    @Override
    public void unsubscribe(Object subscriber) {
        for (String key : config.findEventTypesFor(subscriber.getClass())) {
            subscribers.remove(key, subscriber);
        }
    }

    protected Executor getExecutorFor(String key, Class<?> subscriber) {
        Executor executor = config.findExecutorFor(subscriber, key);
        if (executor == null) {
            executor = defaultExecutor;
        }

        return executor;
    }

    private <T> void post(String key, T event) {
        if (config.needToSave(key)) {
            savedEvents.put(key, event);
        }

        for (Object subscriber : subscribers.get(key)) {
            postToSubscriber(subscriber, key, event);
        }
    }

    @Override
    public void post(Object event) {
        post(ChannelKey.get(event.getClass()), event);
    }

    private void postToSubscriber(Object subscriber, String key, Object event) {
        getExecutorFor(key, subscriber.getClass()).execute(new PostEvent(subscriber, event, key));
    }

    private class PostEvent implements Runnable {

        private final Object subscriber;
        private final Object event;
        private final String key;

        private PostEvent(Object subscriber, Object event, String key) {
            this.subscriber = subscriber;
            this.event = event;
            this.key = key;
        }

        @Override
        public void run() {
            try {
                post(subscriber, event, key);
            } catch (Throwable throwable) {
                if (!(event instanceof Throwable)) {
                    post(throwable);
                }
            }
        }
    }
}
