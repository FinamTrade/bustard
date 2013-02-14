package ru.finam.bustard.codegen;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ru.finam.bustard.Bustard;
import ru.finam.bustard.Executor;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class BustardEmitter {

    private final String implName;
    private final String pkgName;
    private final Class<? extends Bustard> supertype;
    private static final String INDENT = "    ";

    private Multimap<String, String[]> subscribers = HashMultimap.create();
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

    public void addSubscriber(String eventTypeName,
                              String subscriberTypeName,
                              String methodName,
                              String executorQualifier) {
        subscribers.put(eventTypeName, new String[] { subscriberTypeName, methodName, executorQualifier });
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
        writer.write("protected void initialize(Config config) {\n");
        for (String executeQualifier : executors.keySet()) {
            emitIndent(writer, 2);
            writer.write(String.format("config.addExecuteQualifier(\"%s\", %s.class);\n",
                    executeQualifier,
                    executors.get(executeQualifier)));
        }
        for (String eventTypeName : subscribers.keySet()) {
            for (String[] data : subscribers.get(eventTypeName)) {
                String subscriberTypeName = data[0];
                String executorQualifier = data[2];

                emitIndent(writer, 2);
                if (executorQualifier == null) {
                    writer.write(String.format("config.put(%s.class, %s.class);\n",
                            subscriberTypeName,
                            eventTypeName));
                } else {
                    writer.write(String.format("config.put(%s.class, %s.class, \"%s\");\n",
                            subscriberTypeName,
                            eventTypeName,
                            executorQualifier));
                }
            }
        }
        emitIndent(writer, 1);
        writer.write("}\n\n");

        emitIndent(writer, 1);
        writer.write("@Override\n");
        emitIndent(writer, 1);
        writer.write("protected void post(Object subscriber, Object event) throws Throwable {\n");
        for (String eventTypeName : subscribers.keySet()) {
            emitIndent(writer, 2);
            writer.write(String.format("if (event instanceof %s) {\n",
                    eventTypeName));

            for (String[] data : subscribers.get(eventTypeName)) {
                String subscriberTypeName = data[0];
                String methodName = data[1];

                emitIndent(writer, 3);
                writer.write(String.format("if (subscriber instanceof %s) {\n",
                        subscriberTypeName));

                emitIndent(writer, 4);
                writer.write(String.format("((%s) subscriber).%s((%s) event);\n",
                        subscriberTypeName,
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
