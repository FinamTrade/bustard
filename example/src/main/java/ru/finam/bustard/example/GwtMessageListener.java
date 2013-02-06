package ru.finam.bustard.example;

import com.google.gwt.user.client.Window;
import net.engio.mbassy.listener.Listener;

public class GwtMessageListener {

    @Listener
    public void handleMessage(MessageEvent event) {
        Window.alert(event.getMessage());
    }
}
