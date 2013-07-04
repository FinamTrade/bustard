package ru.finam.bustard.test;

import ru.finam.bustard.Consumes;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ylevin
 * Date: 02.07.13
 */
public class BaseListener {

    List<String> baseStrings = new ArrayList<String>();

    @Consumes
    public void listen(String str) {
        baseStrings.add(str);
    }
}
