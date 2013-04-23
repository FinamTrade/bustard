package ru.finam.bustard.test;

import ru.finam.bustard.Consumes;

import java.util.ArrayList;
import java.util.List;

public class GenericListenerWithTopic {

    List<String> strings = new ArrayList<String>();

    @Consumes(topic = "SomeTopic")
    public void listenInTopic(List<String> strings) {
        this.strings.addAll(strings);
    }
}
