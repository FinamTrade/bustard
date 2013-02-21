package ru.finam.bustard.gwt;

import com.google.common.collect.HashMultimap;
import ru.finam.bustard.AbstractBustard;
import ru.finam.bustard.ChannelKey;

import java.util.HashMap;

public abstract class AbstractGwtBustard extends AbstractBustard {
    public AbstractGwtBustard() {
        super(new GwtExecutor(), HashMultimap.<ChannelKey, Object>create(), new HashMap<ChannelKey, Object>());
    }
}
