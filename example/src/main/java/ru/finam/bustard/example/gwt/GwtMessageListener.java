package ru.finam.bustard.example.gwt;

import com.google.gwt.user.client.Window;
import ru.finam.bustard.Listener;
import ru.finam.bustard.example.MessageEvent;

public class GwtMessageListener {

    @Listener
    public void handleMessage(MessageEvent event) {
        Window.alert(event.getMessage());
    }
}
