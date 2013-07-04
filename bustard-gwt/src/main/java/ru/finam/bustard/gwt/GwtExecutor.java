package ru.finam.bustard.gwt;

import ru.finam.bustard.Executor;

public class GwtExecutor implements Executor {

    @Override
    public void execute(final Runnable runnable) {
        setTimeout(runnable, 0);
    }

    //TODO elemental-gwt Timer should be used instead but currently it has bugs
    private static native void setTimeout(final Runnable runnable, final int timeout) /*-{
        $wnd.setTimeout($entry(function() {
            runnable.@java.lang.Runnable::run()();
        }), 0);
    }-*/;
}
