package ru.finam.bustard.codegen;

import ru.finam.bustard.Bustard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ChannelsFinder implements ChannelsConsts {

    public static final String FILE_PATH = BUSTARD_PACKAGE_NAME.replace('.', '/');

    public static List<String> retrieveChannelKeys() throws IOException {
        ArrayList<String> channelFileNames = ClasspathFileRetriever.retrieveFileNames(Pattern.compile(".*channels.*bustard"), FILE_PATH);
        List<String> tmp = FileLinesParser.retrieveResources(Bustard.class.getClassLoader(), channelFileNames);
        return tmp;
    }
}
