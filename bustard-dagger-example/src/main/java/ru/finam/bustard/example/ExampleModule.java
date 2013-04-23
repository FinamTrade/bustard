package ru.finam.bustard.example;

import dagger.Module;
import dagger.Provides;
import ru.finam.bustard.Bustard;
import ru.finam.bustard.ChannelModule;
import ru.finam.bustard.java.BustardImpl;

import javax.inject.Singleton;

@Module(
        entryPoints = ChannelHolder.class,
        includes = ChannelModule.class
)
public class ExampleModule {

    @Provides
    @Singleton
    public Bustard provideBustard() {
        Bustard bustard = new BustardImpl();
        bustard.initialize();
        return bustard;
    }
}
