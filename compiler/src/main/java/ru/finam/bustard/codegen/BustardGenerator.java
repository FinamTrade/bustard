package ru.finam.bustard.codegen;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class BustardGenerator {

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

    public void generate(ProcessingEnvironment environment) throws IOException {
        Set<Element> origin = new HashSet<Element>();
        for (TypeElement eventType : events.keySet()) {
            for (ExecutableElement listenerMethod : events.get(eventType)) {
                TypeElement subscriberType = (TypeElement) listenerMethod.getEnclosingElement();
                origin.add(subscriberType);
            }
            origin.add(eventType);
        }

        FileObject fileObject = environment.getFiler().createResource(
                StandardLocation.SOURCE_OUTPUT,
                "ru.finam.bustard", "subscribers.txt",
                origin.toArray(new Element[origin.size()]));

        Writer writer = fileObject.openWriter();

        try {
            writeSubscribers(writer);
        } finally {
            writer.close();
        }
    }

    private void writeSubscribers(Writer writer) throws IOException {
        for (TypeElement eventType : events.keySet()) {
            for (ExecutableElement listenerMethod : events.get(eventType)) {
                TypeElement subscriberType = (TypeElement) listenerMethod.getEnclosingElement();
                writer.write(String.format("%s %s %s\n",
                        subscriberType.getQualifiedName().toString(),
                        listenerMethod.getSimpleName().toString(),
                        eventType.getQualifiedName().toString()));
            }
        }
    }

    private void writeBustard(Writer writer) throws IOException {
        writer.write("package ru.finam.bustard;\n\n");

        writer.write("import com.google.common.collect.Multimap;\n\n");

        writer.write("public class BustardImpl extends AbstractBustard {\n\n");

        writer.write("    @Override\n");
        writer.write("    void initialize(Multimap<String, String> eventTypes) {\n");
        for (TypeElement eventType : events.keySet()) {
            for (ExecutableElement listenerMethod : events.get(eventType)) {
                TypeElement subscriberType = (TypeElement) listenerMethod.getEnclosingElement();

                writer.write(String.format("        eventTypes.put(\"class %s\", \"class %s\");\n",
                        subscriberType.getQualifiedName().toString(),
                        eventType.getQualifiedName().toString()));
            }
        }
        writer.write("    }\n\n");

        writer.write("    @Override\n");
        writer.write("    void post(Object listener, Object event) throws Throwable {\n");
        for (TypeElement eventType : events.keySet()) {
            writer.write(String.format("        if (event instanceof %s) {\n",
                    eventType.getQualifiedName().toString()));

            for (ExecutableElement listenerMethod : events.get(eventType)) {
                TypeElement subscriberType = (TypeElement) listenerMethod.getEnclosingElement();

                writer.write(String.format("            if (listener instanceof %s) {\n",
                        subscriberType.getQualifiedName().toString()));

                writer.write(String.format("                ((%s) listener).%s((%s) event);\n",
                        subscriberType.getQualifiedName().toString(),
                        listenerMethod.getSimpleName().toString(),
                        eventType.getQualifiedName().toString()));

                writer.write("            }\n");
            }
            writer.write("        }\n");
        }
        writer.write("    }\n\n");

        writer.write("}");
    }
}
