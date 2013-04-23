package ru.finam.bustard.test;

import com.google.gwt.core.client.GWT;
import dagger.Module;
import dagger.Provides;
import ru.finam.bustard.Bustard;
import ru.finam.bustard.ChannelModule;

import javax.inject.Singleton;

@Module(
        entryPoints = ChannelHolder.class,
        includes = ChannelModule.class
)
public class ExampleModule {

    @Provides
    @Singleton
    public Bustard provideBustard() {
        Bustard bustard = GWT.create(Bustard.class);
        bustard.initialize();
        return bustard;
    }
}
