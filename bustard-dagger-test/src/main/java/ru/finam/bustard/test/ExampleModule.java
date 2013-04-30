package ru.finam.bustard.test;

import dagger.Module;
import dagger.Provides;
import ru.finam.bustard.Bustard;
import ru.finam.bustard.ChannelModule;
import ru.finam.bustard.java.BustardFactory;

import javax.inject.Singleton;

@Module(
        entryPoints = ChannelHolder.class,
        includes = ChannelModule.class
)
public class ExampleModule {

    @Provides
    @Singleton
    public Bustard provideBustard() {
        Bustard bustard = BustardFactory.createBustard();
        bustard.initialize();
        return bustard;
    }
}
