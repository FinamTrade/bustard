package ru.finam.bustard.codegen;

import dagger.Module;
import dagger.Provides;
import dagger.internal.codegen.InjectProcessor;
import dagger.internal.codegen.ProvidesProcessor;
import ru.finam.bustard.Consumes;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.*;

@SupportedAnnotationTypes({"javax.inject.Inject", "dagger.Module", "dagger.Provides", "ru.finam.bustard.Consumes"})
public class CompositeProcessor extends AbstractProcessor {

    InjectChannelProcessor injectChannelProcessor = new InjectChannelProcessor();
    InjectProcessor injectProcessor = new InjectProcessor();
    ListenerProcessor listenerProcessor = new ListenerProcessor();
    ProvidesProcessor providesProcessor = new ProvidesProcessor();

    Map<Class<? extends Annotation>, Set<Element>> annotatedElements =
            new HashMap<Class<? extends Annotation>, Set<Element>>();

    Map<TypeElement, Set<Element>> annotatedByTypeElement =
            new HashMap<TypeElement, Set<Element>>();

    List<Class<? extends Annotation>> annotationClasses = new ArrayList<Class<? extends Annotation>>();

    public CompositeProcessor() {
        annotationClasses.add(Inject.class);
        annotationClasses.add(Module.class);
        annotationClasses.add(Provides.class);
        annotationClasses.add(Consumes.class);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Class<? extends Annotation> annotationClass : annotationClasses) {
            Set<Element> set = annotatedElements.get(annotationClass);
            if (set == null) {
                set = new HashSet<Element>();
                annotatedElements.put(annotationClass, set);
            }
            set.addAll(roundEnv.getElementsAnnotatedWith(annotationClass));
        }

        if (roundEnv.processingOver()) {
            RoundEnvironment compositeEnv = new CompositeRoundEnvironment(roundEnv);
            listenerProcessor.init(processingEnv);
            listenerProcessor.process(annotations, compositeEnv);

            injectChannelProcessor.init(processingEnv);
            injectChannelProcessor.process(annotations, compositeEnv);

        }
        return true;
    }

    public class CompositeRoundEnvironment implements RoundEnvironment {

        RoundEnvironment delegated;

        public CompositeRoundEnvironment(RoundEnvironment delegated) {
            this.delegated = delegated;
        }

        @Override
        public boolean processingOver() {
            return delegated.processingOver();
        }

        @Override
        public boolean errorRaised() {
            return delegated.errorRaised();
        }

        @Override
        public Set<? extends Element> getRootElements() {
            return delegated.getRootElements();
        }

        @Override
        public Set<? extends Element> getElementsAnnotatedWith(TypeElement a) {
            return annotatedByTypeElement.get(a);
        }

        @Override
        public Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a) {
            return annotatedElements.get(a);
        }
    }
}
