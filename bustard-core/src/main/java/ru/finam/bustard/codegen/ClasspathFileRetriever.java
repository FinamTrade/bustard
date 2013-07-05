package ru.finam.bustard.codegen;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import ru.finam.bustard.Bustard;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * User: drevis
 * Date: 26.06.13
 */
public class ClasspathFileRetriever {

    public static ArrayList<String> retrieveLines(Pattern pattern, String baseFilePath) throws IOException {
        Enumeration<URL> en = Bustard.class.getClassLoader().getResources(baseFilePath);
        ArrayList<String> lines = Lists.newArrayList();
        while (en.hasMoreElements()) {
            URL url = en.nextElement();
            String path;
            try {
                path = getFilePath(url);
            } catch (Exception e) {
                if ("jar".equals(url.getProtocol())) {
                    JarURLConnection connection = (JarURLConnection) url.openConnection();
                    URL jarUrl = connection.getJarFileURL();
                    try {
                        path = getFilePath(jarUrl);
                    } catch (URISyntaxException ex) {
                        throw new IOException("cannot handle url " + url, ex);
                    }
                } else {
                    path = url.getFile();
                }
            }
            lines.addAll(getResourcesLines(path, pattern));
        }
        return lines;
    }


    private static Collection<String> getResourcesLines(
            final String element,
            final Pattern pattern) throws IOException {
        final ArrayList<String> retval = new ArrayList<String>();
        final File file = new File(element);
        if (file.isDirectory()) {
            retval.addAll(getResourcesLinesFromDirectory(file, pattern));
        } else {
            retval.addAll(getResourcesLinesFromJarFile(file, pattern));
        }
        return retval;
    }

    private static Collection<String> getResourcesLinesFromJarFile(File file, Pattern pattern) throws IOException {
        final ArrayList<String> lines = Lists.newArrayList();
        ZipFile zf = new ZipFile(file);
        final Enumeration e = zf.entries();
        while (e.hasMoreElements()) {
            final ZipEntry ze = (ZipEntry) e.nextElement();
            final String fileName = ze.getName();
            final boolean accept = pattern.matcher(fileName).matches();
            if (accept) {
                lines.addAll(CharStreams.readLines(new InputStreamReader(zf.getInputStream(ze), Charsets.UTF_8)));
            }
        }
        zf.close();
        return lines;
    }

    private static Collection<String> getResourcesLinesFromDirectory(File directory, Pattern pattern) throws IOException {
        ArrayList<String> lines = Lists.newArrayList();
        File[] list = directory.listFiles();
        if (list != null) {
            for (final File file : list) {
                if (file.isDirectory()) {
                    lines.addAll(getResourcesLinesFromDirectory(file, pattern));
                } else {
                    final String fileName = file.getCanonicalPath();
                    final boolean accept = pattern.matcher(fileName).matches();
                    if (accept) {
                        lines.addAll(Resources.readLines(file.toURI().toURL(), Charsets.UTF_8));
                    }
                }
            }
        }
        return lines;
    }

    private static String getFilePath(URL url) throws URISyntaxException {
        URI uri = url.toURI();
        File file = new File(uri);
        return file.getPath();
    }
}
