package ru.finam.bustard.example.gwt;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import ru.finam.bustard.Bustard;
import ru.finam.bustard.example.MessageEvent;

public class ExampleGwtApp implements EntryPoint {

    final Bustard bustard = GWT.create(Bustard.class);

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
