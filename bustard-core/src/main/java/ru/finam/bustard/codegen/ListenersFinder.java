package ru.finam.bustard.codegen;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class ListenersFinder implements Consts {
    public static final String FILE_PATH = BUSTARD_PACKAGE_NAME.replace('.', '/');

    public static List<MethodDescription> retrieveSubscribeMethods() throws IOException {
        List<MethodDescription> result = Lists.newArrayList();
        for (String line : ClasspathFileRetriever.retrieveLines(Pattern.compile(".*listeners.*bustard"), FILE_PATH)) {
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
