package ru.finam.bustard.codegen;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ru.finam.bustard.Consumes;
import ru.finam.bustard.ExecuteQualifier;
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

public class BustardGenerator implements Consts {
    private Multimap<TypeMirror, ExecutableElement> events = HashMultimap.create();

    public static TypeElement mirrorToElement(TypeMirror typeMirror) {
        return (TypeElement) ((DeclaredType) typeMirror).asElement();
    }

    public void addListener(ExecutableElement listenerMethod) {
        List<? extends VariableElement> parameters = listenerMethod.getParameters();

        if (parameters.size() != 1) {
            throw new AssertionError("Consumes method " + listenerMethod.toString() +
                    " must have only one argument.");
        }

        events.put(parameters.get(0).asType(), listenerMethod);
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
        for (AnnotationMirror methodAnnotation : listenerMethod.getAnnotationMirrors()) {
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
        BustardEmitter bustardEmitter = new BustardEmitter(BUSTARD_JAVA_PACKAGE_NAME, BUSTARD_IMPL_NAME, AbstractJavaBustard.class);
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

        for (TypeMirror eventType : events.keySet()) {
            for (ExecutableElement listenerMethod : events.get(eventType)) {
                TypeElement listenerType = (TypeElement) listenerMethod.getEnclosingElement();
                TypeElement qualifierType = getExecuteQualifier(listenerMethod);

                origin.add(listenerType);

                String listenerName = listenerType.getQualifiedName().toString();
                String eventName = eventType.toString();
                String methodName = listenerMethod.getSimpleName().toString();
                String executeQualifierName = null;

                Consumes consumesAnnotation = listenerMethod.getAnnotation(Consumes.class);
                boolean eventOnBinding = consumesAnnotation.eventOnBinding();
                String topic = consumesAnnotation.topic();

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
            origin.add(mirrorToElement(eventType));
        }

        String randString = Long.toHexString(Double.doubleToLongBits(Math.random()));
        String listenersFilename = LISTENERS_FILE_BASE_NAME + "-" + randString + "." + BUSTARD_FILE_EXTENSION;

        FileObject listenersFileObject = environment.getFiler().createResource(
                // TODO: If you're running integration tests via maven, change to CLASS_OUTPUT, but don't commit or deploy it.
                StandardLocation.SOURCE_OUTPUT,
                BUSTARD_PACKAGE_NAME, listenersFilename,
                origin.toArray(new Element[origin.size()]));

        Writer subscribersWriter = listenersFileObject.openWriter();

        try {
            subscribersWriter.write(subscribersInfo.toString());
        } finally {
            subscribersWriter.close();
        }

        if (!"true".equals(environment.getOptions().get("nobustards"))) {
            JavaFileObject bustardFileObject = environment.getFiler().createSourceFile(
                    BUSTARD_JAVA_PACKAGE_NAME + "." + BUSTARD_IMPL_NAME,
                    origin.toArray(new Element[origin.size()]));

            Writer bustardFileWriter = bustardFileObject.openWriter();

            try {
                bustardEmitter.emit(bustardFileWriter);
            } finally {
                bustardFileWriter.close();
            }
        }
    }
}
