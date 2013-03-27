package ru.finam.bustard.example;

import dagger.Module;
import dagger.Provides;
import ru.finam.bustard.Bustard;
import ru.finam.bustard.ChannelModule;
import ru.finam.bustard.java.BustardImpl;

@Module(
        entryPoints = ChannelHolder.class,
        includes = ChannelModule.class
)
public class ExampleModule {

    @Provides
    public Bustard provideBustard(BustardImpl bustardImpl) {
        return bustardImpl;
    }
}
