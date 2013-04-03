package ru.finam.bustard.codegen;

import dagger.Module;
import dagger.Provides;
import ru.finam.bustard.Channel;
import ru.finam.bustard.ChannelKey;
import ru.finam.bustard.ChannelModule;
import ru.finam.bustard.Topic;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.StringTokenizer;

public class ChannelModuleGenerator {

    private int counter = 0;

    public void generate(Set<String> channelKeys, Writer writer) throws IOException {
        writer.write(String.format("package %s;\n\n", ChannelModule.class.getPackage().getName()));
        writer.write(String.format("@%s(complete = false)\n", Module.class.getName()));
        writer.write(String.format("public class %s {\n\n", ChannelModule.class.getSimpleName()));
        for (String key : channelKeys) {
            writeProvideMethod(writer, key, true);
            if (ChannelKey.getTopic(key).isEmpty()) {
                writeProvideMethod(writer, key, false);
            }
        }
        writer.write("}");
    }

    private void writeProvideMethod(Writer writer, String channelKey, boolean withTopic) throws IOException {
        writer.write(String.format("    @%s\n", Provides.class.getName()));

        String topic = ChannelKey.getTopic(channelKey);
        String eventTypeName = ChannelKey.getTypeName(channelKey);

        if (withTopic) {
            writer.write(String.format("    @%s(\"%s\")\n",
                    Topic.class.getName(), topic));
        }
        writer.write(String.format("    public %s<%s> provideChannel%d(Bustard bustard) {\n",
                Channel.class.getName(), eventTypeName, counter++));
        writer.write(String.format("        return bustard.getChannelFor(\"%s\");\n", channelKey));
        writer.write(String.format("    }\n\n"));
    }

    private String generateMethodSuffix(String eventTypeName, String topic) {
        StringTokenizer tokenizer = new StringTokenizer(eventTypeName, "<,> ", true);
        StringBuilder result = new StringBuilder();
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("<")) {
                result.append("Of");
            } else if (token.equals(",")) {
                result.append("And");
            } else if (token.equals(">") || token.equals(" ")) {
                // Do Nothing
            } else {
                result.append(token.substring(token.lastIndexOf('.') + 1));
            }
        }
        return result.toString() + topic;
    }
}
