package ru.finam.bustard.example.gwt;

import com.google.gwt.user.client.Window;
import net.engio.mbassy.listener.Listener;
import ru.finam.bustard.example.MessageEvent;

public class GwtMessageListener {

    @Listener
    public void handleMessage(MessageEvent event) {
        Window.alert(event.getMessage());
    }
}
