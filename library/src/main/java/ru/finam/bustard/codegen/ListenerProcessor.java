package ru.finam.bustard.codegen;

import net.engio.mbassy.listener.Listener;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes("net.engio.mbassy.listener.Listener")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ListenerProcessor extends AbstractProcessor {

    private BustardGenerator generator = new BustardGenerator();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element elem : roundEnv.getElementsAnnotatedWith(Listener.class)) {
            generator.addListener((ExecutableElement) elem);
        }

        if (roundEnv.processingOver()) {
            try {
                generator.generate(processingEnv);
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Generate processing failed: " + e.toString());
            }
        }
        return true;
    }
}
