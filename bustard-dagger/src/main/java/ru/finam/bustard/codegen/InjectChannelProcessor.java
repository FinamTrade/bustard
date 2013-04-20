package ru.finam.bustard.codegen;

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
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@SupportedAnnotationTypes("javax.inject.Inject")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class InjectChannelProcessor extends AbstractProcessor implements ChannelsConsts {

    private final Set<String> channelKeys = new TreeSet<String>();
    private final Set<TypeElement> originTypes = new HashSet<TypeElement>();

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
            try {
                FileObject channelsFileObject = processingEnv.getFiler().createResource(
                        StandardLocation.CLASS_OUTPUT,
                        BUSTARD_PACKAGE_NAME, CHANNELS_FILE_NAME,
                        originTypes.toArray(new Element[originTypes.size()]));

                Writer channelsWriter = channelsFileObject.openWriter();

                try {
                    for (String key : channelKeys) {
                        channelsWriter.write(key + "\n");
                    }
                } finally {
                    channelsWriter.close();
                }

                if (!"true".equals(processingEnv.getOptions().get("nobustards"))) {
                    channelKeys.addAll(ChannelsFinder.retrieveChannelKeys());

                    ChannelModuleGenerator generator = new ChannelModuleGenerator();

                    JavaFileObject channelModuleFile = processingEnv.getFiler().createSourceFile(
                            BUSTARD_PACKAGE_NAME + "." + CHANNEL_MODULE_NAME,
                            originTypes.toArray(new TypeElement[originTypes.size()]));

                    Writer channelModuleWriter = channelModuleFile.openWriter();
                    try {
                        generator.generate(channelKeys, channelModuleWriter);
                    } finally {
                        channelModuleWriter.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private void addIfChannel(TypeElement type, VariableElement element) {
        String typeName = element.asType().toString();
        if(!typeName.startsWith(Channel.class.getName())) {
            return;
        }

        String topic = "";
        Topic topicAnnotation = element.getAnnotation(Topic.class);
        if (topicAnnotation != null) {
            if (topicAnnotation.value().isEmpty()) {
                throw new RuntimeException("Topic must contains not empty value.");
            }
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
