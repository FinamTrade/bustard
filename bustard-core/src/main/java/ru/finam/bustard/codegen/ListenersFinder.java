package ru.finam.bustard.codegen;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import ru.finam.bustard.Bustard;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class ListenersFinder implements Consts {

    public static final Pattern LINE_PATTERN =
            Pattern.compile("^([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]* " +
                    "[a-zA-Z_$][a-zA-Z\\d_$]* ([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]* " +
                    "([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]* (true|false) [a-zA-Z\\d]*$");

    public static final String FILE_PATH = LISTENERS_PACKAGE_NAME.replace('.', '/') +
            "/" + LISTENERS_FILE_NAME;

    public static Iterable<MethodDescription> retrieveSubscribeMethods() throws IOException {
        ClassLoader bustardClassLoader = Bustard.class.getClassLoader();
        @SuppressWarnings("unchecked")
        final List<URL> urls = Lists.newArrayList(Iterators.forEnumeration(
                bustardClassLoader.getResources(FILE_PATH)));
        return new Iterable<MethodDescription>() {
            @Override
            public Iterator<MethodDescription> iterator() {
                return new MethodIterator(urls);
            }
        };
    }

    private static String readEntireFile(URL fileUrl) throws URISyntaxException, IOException {
        File file = new File(fileUrl.toURI());
        FileReader reader = new FileReader(file);
        StringBuilder contents = new StringBuilder();
        char[] buffer = new char[4096];
        int read = 0;
        do {
            contents.append(buffer, 0, read);
            read = reader.read(buffer);
        } while (read >= 0);
        return contents.toString();
    }

    public static class MethodIterator implements Iterator<MethodDescription> {

        private Iterator<URL> urls;

        private StringTokenizer tokenizer = null;
        private String nextLine = null;

        public MethodIterator(List<URL> urls) {
            this.urls = urls.iterator();
        }

        @Override
        public boolean hasNext() {
            try {
                while (nextLine == null || !LINE_PATTERN.matcher(nextLine).matches()) {
                    while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                        if (!urls.hasNext()) {
                            return false;
                        }

                        tokenizer = new StringTokenizer(readEntireFile(urls.next()), "\n\r");
                    }
                    nextLine = tokenizer.nextToken();
                }

                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public MethodDescription next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            String[] tokens = nextLine.split(" ");
            nextLine = null;

            String executeQualifier = tokens[3].equals("null") ? null : tokens[3];
            boolean eventOnBinding = Boolean.parseBoolean(tokens[4]);
            String topic = tokens.length < 6 ? "" : tokens[5];
            return new MethodDescription(tokens[0], tokens[1], tokens[2], executeQualifier, eventOnBinding, topic);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}