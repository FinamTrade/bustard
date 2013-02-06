package ru.finam.bustard.example;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import ru.finam.bustard.Bustard;
import ru.finam.bustard.BustardImpl;

public class ExampleGwtApp implements EntryPoint {

    final Bustard bustard = new BustardImpl();

    @Override
    public void onModuleLoad() {
        bustard.initialize();

        GwtMessageListener listener = new GwtMessageListener();

        bustard.subscribe(listener);

        Button btn = Button.wrap(Document.get().getElementById("sendMessage"));
        btn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                bustard.post(new MessageEvent("Hello World!"));
            }
        });
        btn.setEnabled(true);
    }
}
