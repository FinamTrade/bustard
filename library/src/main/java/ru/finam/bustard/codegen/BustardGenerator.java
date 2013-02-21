package ru.finam.bustard.codegen;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ru.finam.bustard.ExecuteQualifier;
import ru.finam.bustard.Listener;
import ru.finam.bustard.java.AbstractJavaBustard;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BustardGenerator {

    public static final String PACKAGE_NAME = "ru.finam.bustard.java";
    public static final String IMPL_NAME = "BustardImpl";
    public static final String LISTENERS_PACKAGE_NAME = "ru.finam.bustard";
    public static final String LISTENERS_FILE_NAME = "listeners.bustard";

    private Multimap<TypeElement, ExecutableElement> events = HashMultimap.create();

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
        return executeQualifierType;
    }

    public void generate(ProcessingEnvironment environment) throws IOException {
        Set<Element> origin = new HashSet<Element>();
        BustardEmitter bustardEmitter = new BustardEmitter(PACKAGE_NAME, IMPL_NAME, AbstractJavaBustard.class);
        StringBuilder subscribersInfo = new StringBuilder();

        for (MethodDescription description : ListenersFinder.retrieveSubscribeMethods()) {
            bustardEmitter.addSubscriber(description);

            if (description.getExecuteQualifierName() != null) {
                TypeElement qualifierType = environment.getElementUtils()
                        .getTypeElement(description.getExecuteQualifierName());

                String executorTypeName = extractExecutorType(qualifierType).toString();
                bustardEmitter.addExecutor(qualifierType.getQualifiedName().toString(), executorTypeName);
            }
        }

        for (TypeElement eventType : events.keySet()) {
            for (ExecutableElement listenerMethod : events.get(eventType)) {
                TypeElement listenerType = (TypeElement) listenerMethod.getEnclosingElement();
                TypeElement qualifierType = getExecuteQualifier(listenerMethod);

                origin.add(listenerType);

                String listenerName = listenerType.getQualifiedName().toString();
                String eventName = eventType.getQualifiedName().toString();
                String methodName = listenerMethod.getSimpleName().toString();
                String executeQualifierName = null;

                Listener listenerAnnotation = listenerMethod.getAnnotation(Listener.class);
                boolean eventOnBinding = listenerAnnotation.eventOnBinding();
                String topic = listenerAnnotation.topic();

                if (qualifierType != null) {
                    origin.add(qualifierType);
                    executeQualifierName = qualifierType.getQualifiedName().toString();
                    String executorTypeName = extractExecutorType(qualifierType).toString();
                    bustardEmitter.addExecutor(executeQualifierName, executorTypeName);
                }

                bustardEmitter.addSubscriber(new MethodDescription(
                        listenerName, methodName, eventName, executeQualifierName, eventOnBinding, topic));

                subscribersInfo.append(String.format("%s %s %s %s %b %s\n",
                        listenerName, methodName, eventName, executeQualifierName, eventOnBinding, topic));
            }
            origin.add(eventType);
        }

        FileObject lestenersFileObject = environment.getFiler().createResource(
                StandardLocation.CLASS_OUTPUT,
                LISTENERS_PACKAGE_NAME, LISTENERS_FILE_NAME,
                origin.toArray(new Element[origin.size()]));

        Writer subscribersWriter = lestenersFileObject.openWriter();

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
