package ru.finam.bustard.codegen;

import ru.finam.bustard.Bustard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListenersFinder implements Consts {

    public static final String FILE_PATH = LISTENERS_PACKAGE_NAME.replace('.', '/') +
            "/" + LISTENERS_FILE_NAME;

    public static List<MethodDescription> retrieveSubscribeMethods() throws IOException {
        List<String> lines =
                FileLinesParser.retrieveResource(Bustard.class.getClassLoader(), FILE_PATH);

        List<MethodDescription> result = new ArrayList<MethodDescription>();
        for (String line : lines) {
            result.add(parseLine(line));
        }
        return result;
    }

    private static MethodDescription parseLine(String line) {
        String[] tokens = line.split(" ");
        String listenerName = tokens[0];
        String methodName = tokens[1];
        String eventName = tokens[2];
        String executeQualifier = tokens[3];
        if ("null".equals(executeQualifier)) {
            executeQualifier = null;
        }
        boolean eventOnBinding = Boolean.parseBoolean(tokens[4]);
        String topic = tokens.length < 6 ? "" : tokens[5];
        return new MethodDescription(listenerName, methodName, eventName, executeQualifier, eventOnBinding, topic);
    }
}
