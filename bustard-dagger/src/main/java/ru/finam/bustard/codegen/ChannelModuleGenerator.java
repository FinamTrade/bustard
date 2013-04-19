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

public class ChannelModuleGenerator implements ChannelsConsts {

    private int counter = 0;

    public void generate(Set<String> channelKeys, Writer writer) throws IOException {
        writer.write(String.format("package %s;\n\n", BUSTARD_PACKAGE_NAME));
        writer.write(String.format("@%s(complete = false)\n", Module.class.getName()));
        writer.write(String.format("public class %s {\n\n", CHANNEL_MODULE_NAME));
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
}
