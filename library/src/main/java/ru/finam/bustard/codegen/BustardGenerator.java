package ru.finam.bustard.codegen;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class BustardGenerator {

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

    public void generate(ProcessingEnvironment environment) throws IOException {
        JavaFileObject fileObject = environment.getFiler().createSourceFile("ru.finam.bustard.BustardImpl");

        Writer writer = fileObject.openWriter();
        try {
            writeBustard(writer);
        } finally {
            writer.close();
        }
    }

    private void writeBustard(Writer writer) throws IOException {
        writer.write("package ru.finam.bustard;\n\n");

        writer.write("import com.google.common.collect.Multimap;\n\n");

        writer.write("public class BustardImpl extends AbstractBustard {\n\n");

        writer.write("    @Override\n");
        writer.write("    void initialize(Multimap<Class<?>, Class<?>> eventTypes) {\n");
        for (TypeElement eventType : events.keys()) {
            for (ExecutableElement listenerMethod : events.get(eventType)) {
                TypeElement subscriberType = (TypeElement) listenerMethod.getEnclosingElement();

                writer.write(String.format("        eventTypes.put(%s.class, %s.class);\n",
                        subscriberType.getQualifiedName().toString(),
                        eventType.getQualifiedName().toString()));
            }
        }
        writer.write("    }\n\n");

        writer.write("    @Override\n");
        writer.write("    void post(Object listener, Object event) throws Throwable {\n");
        for (TypeElement eventType : events.keys()) {
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
