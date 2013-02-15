package ru.finam.bustard.codegen;


import org.apache.commons.collections.EnumerationUtils;
import ru.finam.bustard.BustardImpl;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class SubscribersFinder {

    public static final Pattern LINE_PATTERN =
            Pattern.compile("^([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]* " +
                    "[a-zA-Z_$][a-zA-Z\\d_$]* ([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]* " +
                    "([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*$");

    public static final String FILE_PATH = BustardGenerator.PACKAGE_NAME.replace('.', '/') +
            "/" + BustardGenerator.SUBSCRIBERS_FILE_NAME;

    public static Iterable<MethodDescription> retrieveSubscribersInfo() throws IOException {
        ClassLoader bustardClassLoader = BustardImpl.class.getClassLoader();
        @SuppressWarnings("unchecked")
        final List<URL> urls = EnumerationUtils.toList(
                bustardClassLoader.getResources(FILE_PATH));
        return new Iterable<MethodDescription>() {
            @Override
            public Iterator<MethodDescription> iterator() {
                return new LineIterator(urls);
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

    public static class LineIterator implements Iterator<MethodDescription> {

        private Iterator<URL> urls;

        private StringTokenizer tokenizer = null;
        private String nextLine = null;

        public LineIterator(List<URL> urls) {
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
            return new MethodDescription(tokens[0], tokens[1], tokens[2], executeQualifier);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
