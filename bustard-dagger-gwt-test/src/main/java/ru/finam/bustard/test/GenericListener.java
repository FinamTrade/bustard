package ru.finam.bustard.test;

import ru.finam.bustard.Consumes;

import java.util.ArrayList;
import java.util.List;

public class GenericListener extends BaseListener {

    List<String> strings = new ArrayList<String>();

    @Consumes
    public void listen(List<String> strings) {
        this.strings.addAll(strings);
    }
}
