package ru.finam.bustard.codegen;

import dagger.Module;
import dagger.Provides;
import ru.finam.bustard.Channel;
import ru.finam.bustard.ChannelKey;
import ru.finam.bustard.ChannelModule;
import ru.finam.bustard.Topic;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

@SupportedAnnotationTypes("javax.inject.Inject")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class InjectChannelProcessor extends AbstractProcessor {

    private Set<String> channelKeys = new HashSet<String>();

    private Set<TypeElement> originTypes = new HashSet<TypeElement>();

    private int channelsCounter = 0;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Inject.class)) {
            if (element.getKind() == ElementKind.FIELD) {
                addIfChannel((TypeElement) element.getEnclosingElement(), (VariableElement) element);
            } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement) element;
                for (VariableElement parameter  : constructor.getParameters()) {
                    addIfChannel((TypeElement) constructor.getEnclosingElement(), parameter);
                }
            }
        }
        if (roundEnv.processingOver()) {
            Writer writer;
            try {
                JavaFileObject file = processingEnv.getFiler().createSourceFile(
                        ChannelModule.class.getName(),
                        originTypes.toArray(new TypeElement[originTypes.size()]));

                writer = file.openWriter();

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
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
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
                Channel.class.getName(), eventTypeName, channelsCounter++));
        writer.write(String.format("        return bustard.getChannelFor(\"%s\");\n", channelKey));
        writer.write(String.format("    }\n\n"));
    }

    private void addIfChannel(TypeElement type, VariableElement element) {
        String typeName = element.asType().toString();
        if(!typeName.startsWith(Channel.class.getName())) {
            return;
        }

        String topic = "";
        Topic topicAnnotation = element.getAnnotation(Topic.class);
        if (topicAnnotation != null) {
            topic = topicAnnotation.value();
        }

        int beginIndex = typeName.indexOf('<');
        int endIndex = typeName.lastIndexOf('>');

        if (beginIndex < 0) {
            throw new RuntimeException("Must specify generic parameter of Channel type.");
        }

        String eventType = typeName.substring(beginIndex + 1, endIndex);

        channelKeys.add(ChannelKey.get(eventType, topic));
        originTypes.add(type);
    }
}
