package ru.finam.bustard;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GwtAdapterGenerator {
    public GwtAdapterGenerator() {
    }

    private final Set<String> generatedHandlers = new HashSet<String>();

    private final static String HANDLER_SUFFIX = "$GwtHandler";
    private final static String EVENT_ADAPTER_SUFFIX = "$GwtEventAdapter";
    private final static String HANDLER_ADAPTER_SUFFIX = "$GwtHandlerAdapter";



    private static String packageName(TypeElement eventType) {
        throw new UnsupportedOperationException();
    }

    private static String simpleName(TypeElement typeElement) {
        throw new UnsupportedOperationException();
    }

    private static String generatedClassName(TypeElement type, String suffix) {
        throw new UnsupportedOperationException();
    }

    private static String generatedClassSimpleName(TypeElement type, String suffix) {
        throw new UnsupportedOperationException();
    }

    private static String className(TypeElement element) {
        throw new UnsupportedOperationException();
    }

    private static TypeElement mirrorToElement(TypeMirror mirror) {
        throw new UnsupportedOperationException();
    }

    public void generateEventAdapter(ProcessingEnvironment processingEnv,
                                            TypeElement eventType) throws IOException {
        String packageName = packageName(eventType);
        String eventName = className(eventType);
        String handlerName = generatedClassSimpleName(eventType, HANDLER_SUFFIX);
        String handlerCanonicalName = generatedClassName(eventType, HANDLER_SUFFIX);
        String eventAdapterName = generatedClassSimpleName(eventType, EVENT_ADAPTER_SUFFIX);
        String eventAdapterCanonicalName = generatedClassSimpleName(eventType, EVENT_ADAPTER_SUFFIX);

        JavaFileObject eventAdapterFile = processingEnv.getFiler().createClassFile(eventAdapterCanonicalName);
        writeEventAdapter(eventAdapterFile, packageName, eventName, eventAdapterName, handlerName);

        JavaFileObject handlerFile = processingEnv.getFiler().createClassFile(handlerCanonicalName);
        writeHandler(handlerFile, packageName, eventName, eventAdapterName, handlerName);
    }

    public void generateHandlerAdapter(ProcessingEnvironment processingEnv,
                                              TypeElement listenerType,
                                              Set<ExecutableElement> listenerMethods) throws IOException {

        String packageName = packageName(listenerType);
        String listenerName = simpleName(listenerType);
        String handlerAdapterName = generatedClassSimpleName(listenerType, HANDLER_ADAPTER_SUFFIX);
        String handlerAdapterCanonicalName = generatedClassName(listenerType, HANDLER_ADAPTER_SUFFIX);

        List<String> handlerNames = new ArrayList<String>();
        List<String> eventAdapterNames = new ArrayList<String>();
        List<String> eventNames = new ArrayList<String>();
        List<String> methodNames = new ArrayList<String>();

        List<TypeElement> eventTypes = new ArrayList<TypeElement>();

        for (ExecutableElement method : listenerMethods) {
            List<? extends VariableElement> parameters = method.getParameters();

            if (parameters.size() != 1) {
                throw new IllegalArgumentException("Listener method: " + method.toString() +
                        " must have only one argument.");
            }

            TypeElement parameterType = mirrorToElement(parameters.get(0).asType());
            eventTypes.add(parameterType);

            eventNames.add(simpleName(parameterType));
            handlerNames.add(generatedClassName(parameterType, HANDLER_SUFFIX));
            eventAdapterNames.add(generatedClassName(parameterType, EVENT_ADAPTER_SUFFIX));
            methodNames.add(method.getSimpleName().toString());
        }

        JavaFileObject handlerAdapterFile = processingEnv.getFiler().createClassFile(handlerAdapterCanonicalName);

        writeHandlerAdapter(handlerAdapterFile, packageName, listenerName, handlerAdapterName,
                handlerNames, eventAdapterNames, eventNames, methodNames);

        for (TypeElement eventType : eventTypes) {
            String eventHandlerName = eventType.getQualifiedName().toString();
            if (!generatedHandlers.contains(eventHandlerName)) {
                generateEventAdapter(processingEnv, eventType);
            } else {
                generatedHandlers.add(eventHandlerName);
            }
        }
    }

    private static void writeHandlerAdapter(JavaFileObject handlerAdapterFile,
                                            String packageName,
                                            String listenerName,
                                            String handlerAdapterName,
                                            List<String> handlerNames,
                                            List<String> eventAdapterNames,
                                            List<String> eventNames,
                                            List<String> methodNames) throws IOException {
        Writer writer = handlerAdapterFile.openWriter();

        try {
            writer.write(String.format("package %s;\n\n", packageName));

            writer.write(String.format("public class %s implements\n", handlerAdapterName));
            writer.write(String.format("        "));
            boolean first = true;
            for (String handlerName : handlerNames) {
                if (first) {
                    first = false;
                } else {
                    writer.write(", ");
                }
                writer.write(handlerName);
            }
            writer.write(" {\n\n");

            writer.write(String.format("    private %s listener;\n\n", listenerName));

            writer.write(String.format("    public %s(%s listener) {\n", handlerAdapterName, listenerName));
            writer.write(String.format("        this.listener = listener;\n"));
            writer.write(String.format("    }\n\n"));

            for (int i = 0; i < eventAdapterNames.size(); i++) {
                writer.write(String.format("    public void on%s(%s gwtEvent) {\n",
                        eventNames.get(i), eventAdapterNames.get(i)));
                writer.write(String.format("        listener.%s(gwtEvent.getEvent());\n", methodNames.get(0)));
                writer.write(String.format("    }\n\n"));
            }

            writer.write("}");
        } finally {
            writer.close();
        }
    }

    private static void writeHandler(JavaFileObject handlerFile,
                                     String packageName,
                                     String eventName,
                                     String eventAdapterName,
                                     String handlerName) throws IOException {

        Writer writer = handlerFile.openWriter();

        try {
            writer.write(String.format("package %s;\n\n", packageName));

            writer.write(String.format("import %s;\n\n", EventHandler.class.getCanonicalName()));

            writer.write(String.format("public interface %s extends EventHandler {\n", handlerName));
            writer.write(String.format("    void on%s(%s event);\n", eventName, eventAdapterName));
            writer.write(String.format("}\n"));

        } finally {
            writer.close();
        }
    }

    public static void writeEventAdapter(JavaFileObject eventAdapterFile,
                                         String packageName,
                                         String eventName,
                                         String eventAdapterName,
                                         String handlerName) throws IOException {

        Writer writer = eventAdapterFile.openWriter();

        try {
            writer.write(String.format("package %s;\n\n", packageName));

            writer.write(String.format("import %s;\n\n", GwtEvent.class.getCanonicalName()));

            writer.write(String.format("public class %s extends GwtEvent<%s> {\n",
                    eventAdapterName, handlerName));
            writer.write(String.format("    public static GwtEvent.Type<%s> TYPE = \n", handlerName));
            writer.write(String.format("        new GwtEvent.Type<%s>();\n\n", handlerName));

            writer.write(String.format("    private %s event;\n\n", eventName));

            writer.write(String.format("    public %s(%s event) {\n", eventAdapterName, eventName));
            writer.write(String.format("        this.event = event;\n"));
            writer.write(String.format("    }\n\n"));

            writer.write(String.format("    public %s getEvent() {\n", eventName));
            writer.write(String.format("        return event;\n"));
            writer.write(String.format("    }\n\n"));

            writer.write(String.format("    @Override\n"));
            writer.write(String.format("    public GwtEvent.Type<%s> getAssociatedType() {\n", handlerName));
            writer.write(String.format("        return TYPE;\n"));
            writer.write(String.format("    }\n\n"));

            writer.write(String.format("    @Override\n"));
            writer.write(String.format("    protected void dispatch(%s handler) {\n", handlerName));
            writer.write(String.format("        handler.on%s(this)\n", eventName));
            writer.write(String.format("    }\n\n"));

            writer.write(String.format("}"));
        } finally {
            writer.close();
        }
    }
}
