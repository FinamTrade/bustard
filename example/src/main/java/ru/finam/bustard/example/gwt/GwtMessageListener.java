package ru.finam.bustard.example.gwt;

import com.google.gwt.user.client.Window;
import ru.finam.bustard.Consumes;
import ru.finam.bustard.example.MessageEvent;

public class GwtMessageListener {

    @Consumes
    public void handleMessage(MessageEvent event) {
        Window.alert(event.getMessage());
    }
}
