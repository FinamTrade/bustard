package ru.finam.bustard;

import com.google.common.collect.Multimap;

import java.util.Map;

public abstract class AbstractBustard implements Bustard {

    private final Config config;
    private final Executor defaultExecutor;
    private final Multimap<ChannelKey, Object> subscribers;
    private final Map<ChannelKey, Object> savedEvents;

    public AbstractBustard(Executor defaultExecutor,
                           Multimap<ChannelKey, Object> subscribersMap,
                           Map<ChannelKey, Object> eventsMap) {
        if (defaultExecutor == null) {
            throw new NullPointerException("defaultExecutor");
        }
        if (subscribersMap == null) {
            throw new NullPointerException("subscribersMap");
        }
        if (eventsMap == null) {
            throw new NullPointerException("eventsMap");
        }
        this.defaultExecutor = defaultExecutor;
        this.subscribers = subscribersMap;
        this.savedEvents = eventsMap;
        this.config = new Config();
    }

    public void attachExecutors(Executor... executors) {
        config.attachExecutors(executors);
    }

    @Override
    public <T> Channel<T> getChannelFor(final ChannelKey<T> key) {
        return new Channel<T>() {
            @Override
            public void post(T event) {
                AbstractBustard.this.post(key, event);
            }
        };
    }

    protected abstract void initialize(Config config);

    protected abstract void post(Object subscriber, Object event, String topic) throws Throwable;

    @Override
    public void initialize() {
        initialize(config);
    }

    @Override
    public void subscribe(Object subscriber) {
        for (ChannelKey key : config.findEventTypesFor(subscriber.getClass())) {
            if (config.isEventOnBinding(subscriber.getClass(), key) &&
                    savedEvents.containsKey(key)) {
                postToSubscriber(subscriber, key, savedEvents.get(key));
            }
            subscribers.put(key, subscriber);
        }
    }

    @Override
    public void unsubscribe(Object subscriber) {
        for (ChannelKey key : config.findEventTypesFor(subscriber.getClass())) {
            subscribers.remove(key, subscriber);
        }
    }

    protected Executor getExecutorFor(ChannelKey<?> key, Class<?> subscriber) {
        Executor executor = config.findExecutorFor(subscriber, key);
        if (executor == null) {
            executor = defaultExecutor;
        }

        return executor;
    }

    private <T> void post(ChannelKey<T> key, T event) {
        if (config.needToSave(key)) {
            savedEvents.put(key, event);
        }

        for (Object subscriber : subscribers.get(key)) {
            postToSubscriber(subscriber, key, event);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void post(Object event) {
        post(new ChannelKey(event.getClass()), event);
    }

    private void postToSubscriber(Object subscriber, ChannelKey key, Object event) {
        getExecutorFor(key, subscriber.getClass()).execute(new PostEvent(subscriber, event, key.getTopic()));
    }

    private class PostEvent implements Runnable {

        private final Object subscriber;
        private final Object event;
        private final String topic;

        private PostEvent(Object subscriber, Object event, String topic) {
            this.subscriber = subscriber;
            this.event = event;
            this.topic = topic;
        }

        @Override
        public void run() {
            try {
                post(subscriber, event, topic);
            } catch (Throwable throwable) {
                if (!(event instanceof Throwable)) {
                    post(throwable);
                }
            }
        }
    }
}
