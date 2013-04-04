package ru.finam.bustard;

import com.google.gwt.core.client.GWT;
import dagger.DaggerEntryPoint;

public abstract class BustardEntryPoint extends DaggerEntryPoint {

    @Override
    protected Object[] getCustomModules() {
        return new Object[] { GWT.create(ChannelModule.class) };
    }
}