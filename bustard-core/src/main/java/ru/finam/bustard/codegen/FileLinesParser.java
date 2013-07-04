package ru.finam.bustard.codegen;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileLinesParser {

    public static List<String> retrieveResources(ArrayList<String> filePaths) throws IOException {
        List<String> result = new ArrayList<String>();
        for (String filePath : filePaths) {
            try {
                result.addAll(Resources.readLines(new File(filePath).toURI().toURL(), Charsets.UTF_8));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
