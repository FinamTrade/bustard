package ru.finam.bustard.codegen;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ru.finam.bustard.Bustard;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class BustardEmitter {

    private final String implName;
    private final String pkgName;
    private final Class<? extends Bustard> supertype;
    private static final String INDENT = "    ";

    private Multimap<String, MethodDescription> listeners = HashMultimap.create();
    private Map<String, String> executors = new HashMap<String, String>();

    public BustardEmitter(String pkgName, String implSimpleName,
                          Class<? extends Bustard> supertype) {
        this.pkgName = pkgName;
        this.implName = implSimpleName;
        this.supertype = supertype;
    }

    public void addExecutor(String executeQualifier, String executorTypeName) {
        executors.put(executeQualifier, executorTypeName);
    }

    public void addSubscriber(MethodDescription description) {
        listeners.put(description.getEventName(), description);
    }

    private void emitIndent(Writer writer, int level) throws IOException {
        for (int i = 0; i < level; i++) {
            writer.write(INDENT);
        }
    }

    public void emit(Writer writer) throws IOException {
        writer.write(String.format("package %s;\n\n", pkgName));

        writer.write(String.format("public class %s extends %s {\n\n",
                implName, supertype.getCanonicalName()));

        emitIndent(writer, 1);
        writer.write("@Override\n");
        emitIndent(writer, 1);
        writer.write("protected void initialize(ru.finam.bustard.Config config) {\n");
        for (String executeQualifier : executors.keySet()) {
            emitIndent(writer, 2);
            writer.write(String.format("config.addExecuteQualifier(\"%s\", %s.class);\n",
                    executeQualifier,
                    executors.get(executeQualifier)));
        }
        for (String eventTypeName : listeners.keySet()) {
            for (MethodDescription description : listeners.get(eventTypeName)) {
                String listenerTypeName = description.getListenerName();
                String executeQualifier = description.getExecuteQualifierName();

                emitIndent(writer, 2);
                if (executeQualifier == null) {
                    writer.write(String.format("config.put(%s.class, %s.class);\n",
                            listenerTypeName,
                            eventTypeName));
                } else {
                    writer.write(String.format("config.put(%s.class, %s.class, \"%s\");\n",
                            listenerTypeName,
                            eventTypeName,
                            executeQualifier));
                }
            }
        }
        emitIndent(writer, 1);
        writer.write("}\n\n");

        emitIndent(writer, 1);
        writer.write("@Override\n");
        emitIndent(writer, 1);
        writer.write("protected void post(Object subscriber, Object event) throws Throwable {\n");
        for (String eventTypeName : listeners.keySet()) {
            emitIndent(writer, 2);
            writer.write(String.format("if (event instanceof %s) {\n",
                    eventTypeName));

            for (MethodDescription description : listeners.get(eventTypeName)) {
                String listenerTypeName = description.getListenerName();
                String methodName = description.getMethodName();

                emitIndent(writer, 3);
                writer.write(String.format("if (subscriber instanceof %s) {\n",
                        listenerTypeName));

                emitIndent(writer, 4);
                writer.write(String.format("((%s) subscriber).%s((%s) event);\n",
                        listenerTypeName,
                        methodName,
                        eventTypeName));

                emitIndent(writer, 3);
                writer.write("}\n");
            }
            emitIndent(writer, 2);
            writer.write("}\n");
        }
        emitIndent(writer, 1);
        writer.write("}\n\n");

        writer.write("}");
    }
}
