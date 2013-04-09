package ru.finam.bustard.codegen;

import com.google.common.collect.Iterators;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class FileLinesParser {

    public static List<String> retrieveResource(ClassLoader loader, String filePath) throws IOException {
        Iterator<URL> urls = Iterators.forEnumeration(loader.getResources(filePath));
        List<String> result = new ArrayList<String>();
        while (urls.hasNext()) {
            URL url = urls.next();
            InputStreamReader reader = new InputStreamReader(url.openStream());
            try {
                result.addAll(CharStreams.readLines(reader));
            } finally {
                Closeables.closeQuietly(reader);
            }
        }
        return result;
    }

}
