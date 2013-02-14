package ru.finam.bustard.codegen;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import ru.finam.bustard.AbstractBustard;
import ru.finam.bustard.ExecuteQualifier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class BustardGenerator {

    public static final String PACKAGE_NAME = "ru.finam.bustard";
    public static final String IMPL_NAME = "BustardImpl";
    public static final String SUBSCRIBERS_FILE_NAME = "subscribers.bustard";

    private Multimap<TypeElement, ExecutableElement> events = Multimaps.newMultimap(
            new HashMap<TypeElement, Collection<ExecutableElement>>(),
            new Supplier<Collection<ExecutableElement>>() {
                @Override
                public Collection<ExecutableElement> get() {
                    return new HashSet<ExecutableElement>();
                }
            });

    public static TypeElement mirrorToElement(TypeMirror typeMirror) {
        return (TypeElement) ((DeclaredType) typeMirror).asElement();
    }

    public void addListener(ExecutableElement listenerMethod) {
        List<? extends VariableElement> parameters = listenerMethod.getParameters();

        if (parameters.size() != 1) {
            throw new AssertionError("Listener method " + listenerMethod.toString() +
                    " must have only one argument.");
        }

        TypeElement eventType = mirrorToElement(parameters.get(0).asType());
        events.put(eventType, listenerMethod);
    }

    private TypeMirror extractExecutorType(TypeElement qualifierType) {
        for (AnnotationMirror annotation : qualifierType.getAnnotationMirrors()) {
            if (!annotation.getAnnotationType().toString().equals(ExecuteQualifier.class.getName())) {
                continue;
            }

            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e
                    : annotation.getElementValues().entrySet()) {
                if ("value".equals(e.getKey().getSimpleName().toString())) {
                    return (TypeMirror) e.getValue().getValue();
                }
            }
        }
        return null;
    }

    public TypeElement getExecuteQualifier(ExecutableElement listenerMethod) {
        TypeElement executeQualifierType = null;
        for(AnnotationMirror methodAnnotation : listenerMethod.getAnnotationMirrors()) {
            TypeElement annotationType = (TypeElement) methodAnnotation.getAnnotationType().asElement();
            TypeMirror type = extractExecutorType(annotationType);
            if (type == null) {
                continue;
            }

            if (executeQualifierType != null) {
                throw new RuntimeException("Too many execute qualifiers for method: " + listenerMethod);
            }

            executeQualifierType = annotationType;
        }
        return null;
    }

    public void generate(ProcessingEnvironment environment) throws IOException {
        Set<Element> origin = new HashSet<Element>();
        BustardEmitter bustardEmitter = new BustardEmitter(PACKAGE_NAME, IMPL_NAME, AbstractBustard.class);
        StringBuilder subscribersInfo = new StringBuilder();

        for (SubscriberInfo info : SubscribersFinder.retrieveSubscribersInfo()) {
            bustardEmitter.addSubscriber(
                    info.getEventName(),
                    info.getSubscriberName(),
                    info.getMethodName(),
                    info.getExecuteQualifierName());

            if (!"null".equals(info.getExecuteQualifierName())) {
                TypeElement qualifierType = environment.getElementUtils()
                        .getTypeElement(info.getExecuteQualifierName());

                String executorTypeName = extractExecutorType(qualifierType).toString();
                bustardEmitter.addExecutor(qualifierType.getQualifiedName().toString(), executorTypeName);
            }
        }

        for (TypeElement eventType : events.keySet()) {
            for (ExecutableElement listenerMethod : events.get(eventType)) {
                TypeElement subscriberType = (TypeElement) listenerMethod.getEnclosingElement();
                TypeElement qualifierType = getExecuteQualifier(listenerMethod);

                origin.add(subscriberType);

                String subscriberName = subscriberType.getQualifiedName().toString();
                String eventName = eventType.getQualifiedName().toString();
                String methodName = listenerMethod.getSimpleName().toString();
                String executeQualifierName = null;

                if (qualifierType != null) {
                    origin.add(qualifierType);
                    executeQualifierName = qualifierType.getQualifiedName().toString();
                    String executorTypeName = extractExecutorType(qualifierType).toString();
                    bustardEmitter.addExecutor(executeQualifierName, executorTypeName);
                }

                bustardEmitter.addSubscriber(
                        eventName,
                        subscriberName,
                        methodName,
                        executeQualifierName);

                subscribersInfo.append(String.format("%s %s %s %s\n",
                        subscriberName, methodName, eventName, executeQualifierName));
            }
            origin.add(eventType);
        }

        FileObject subscribersFileObject = environment.getFiler().createResource(
                StandardLocation.CLASS_OUTPUT,
                PACKAGE_NAME, SUBSCRIBERS_FILE_NAME,
                origin.toArray(new Element[origin.size()]));

        Writer subscribersWriter = subscribersFileObject.openWriter();

        try {
            subscribersWriter.write(subscribersInfo.toString());
        } finally {
            subscribersWriter.close();
        }

        JavaFileObject bustardFileObject = environment.getFiler().createSourceFile(
                PACKAGE_NAME + "." + IMPL_NAME,
                origin.toArray(new Element[origin.size()]));

        Writer bustardFileWriter = bustardFileObject.openWriter();

        try {
            bustardEmitter.emit(bustardFileWriter);
        } finally {
            bustardFileWriter.close();
        }
    }
}
