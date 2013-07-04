package ru.finam.bustard.codegen;

import ru.finam.bustard.Bustard;

import java.io.File;
import java.io.IOException;
import java.lang.Exception;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * User: drevis
 * Date: 26.06.13
 */
public class ClasspathFileRetriever {

    private static String getFilePath(URL url) throws URISyntaxException {
        URI uri = url.toURI();
        File file = new File(uri);
        return file.getPath();
    }

    public static ArrayList<String> retrieveFileNames(Pattern pattern, String baseFilePath) throws IOException {
        Enumeration<URL> en = Bustard.class.getClassLoader().getResources(baseFilePath);
        ArrayList<String> listenerFiles = new ArrayList<String>();

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
            listenerFiles.addAll(ResourceList.getResources(path, pattern));
        }
        return listenerFiles;
    }

    private static class ResourceList {

        /**
         * for all elements of java.class.path get a Collection of resources Pattern
         * pattern = Pattern.compile(".*"); gets all resources
         *
         * @param pattern the pattern to match
         * @return the resources in the order they are found
         */

        private static Collection<String> getResources(
                final String element,
                final Pattern pattern) {
            final ArrayList<String> retval = new ArrayList<String>();
            final File file = new File(element);
            if (file.isDirectory()) {
                retval.addAll(getResourcesFromDirectory(file, pattern));
            } else {
                retval.addAll(getResourcesFromJarFile(file, pattern));
            }
            return retval;
        }

        private static Collection<String> getResourcesFromJarFile(
                final File file,
                final Pattern pattern) {
            final ArrayList<String> retval = new ArrayList<String>();
            ZipFile zf;
            try {
                zf = new ZipFile(file);
            } catch (final ZipException e) {
                throw new Error(e);
            } catch (final IOException e) {
                throw new Error(e);
            }
            final Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                final ZipEntry ze = (ZipEntry) e.nextElement();
                final String fileName = ze.getName();
                final boolean accept = pattern.matcher(fileName).matches();
                if (accept) {
                    retval.add(fileName);
                }
            }
            try {
                zf.close();
            } catch (final IOException e1) {
                throw new Error(e1);
            }
            return retval;
        }

        private static Collection<String> getResourcesFromDirectory(
                final File directory,
                final Pattern pattern) {
            final ArrayList<String> retval = new ArrayList<String>();
            final File[] fileList = directory.listFiles();
            for (final File file : fileList) {
                if (file.isDirectory()) {
                    retval.addAll(getResourcesFromDirectory(file, pattern));
                } else {
                    try {
                        final String fileName = file.getCanonicalPath();
                        final boolean accept = pattern.matcher(fileName).matches();
                        if (accept) {
                            retval.add(fileName);
                        }
                    } catch (final IOException e) {
                        throw new Error(e);
                    }
                }
            }
            return retval;
        }
    }
}
