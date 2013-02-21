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

    private Map<String, Multimap<String, MethodDescription>> listeners =
            new HashMap<String, Multimap<String, MethodDescription>>();

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
        Multimap<String, MethodDescription> topicMap = listeners.get(description.getTopic());

        if (topicMap == null) {
            topicMap = HashMultimap.create();
            listeners.put(description.getTopic(), topicMap);
        }

        topicMap.put(description.getEventName(), description);
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
        for (String topic : listeners.keySet()) {
            Multimap<String, MethodDescription> topicListeners = listeners.get(topic);
            for (String eventTypeName : topicListeners.keySet()) {
                for (MethodDescription description : topicListeners.get(eventTypeName)) {
                    emitIndent(writer, 2);
                    writer.write(String.format("config.put(%s.class, %s.class, %s, %s, %b);\n",
                            description.getListenerName(),
                            eventTypeName,
                            stringLiteral(description.getTopic()),
                            stringLiteral(description.getExecuteQualifierName()),
                            description.isEventOnBinding()));
                }
            }
        }
        emitIndent(writer, 1);
        writer.write("}\n\n");

        emitIndent(writer, 1);
        writer.write("@Override\n");
        emitIndent(writer, 1);
        writer.write("protected void post(Object subscriber, Object event, String topic) throws Throwable {\n");
        boolean first = true;
        for (String topic : listeners.keySet()) {
            Multimap<String, MethodDescription> topicListeners = listeners.get(topic);

            if (first) {
                first = false;
                emitIndent(writer, 2);
            } else {
                writer.write(" else ");
            }
            writer.write(String.format("if (\"%s\".equals(topic)) {\n", topic));

            for (String eventTypeName : topicListeners.keySet()) {
                emitIndent(writer, 3);
                writer.write(String.format("if (event instanceof %s) {\n",
                        eventTypeName));

                for (MethodDescription description : topicListeners.get(eventTypeName)) {
                    emitIndent(writer, 4);
                    writer.write(String.format("if (subscriber instanceof %s) {\n",
                            description.getListenerName()));

                    emitIndent(writer, 5);
                    writer.write(String.format("((%s) subscriber).%s((%s) event);\n",
                            description.getListenerName(),
                            description.getMethodName(),
                            eventTypeName));

                    emitIndent(writer, 4);
                    writer.write("}\n");
                }
                emitIndent(writer, 3);
                writer.write("}\n");
            }

            emitIndent(writer, 2);
            writer.write("}");
        }
        writer.write("\n");
        emitIndent(writer, 1);
        writer.write("}\n\n");

        writer.write("}");
    }

    private String stringLiteral(String value) {
        return value == null ? String.valueOf((Object) null) : String.format("\"%s\"", value);
    }
}
