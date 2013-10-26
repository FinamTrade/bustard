package ru.finam.bustard.codegen;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ru.finam.bustard.Bustard;
import ru.finam.bustard.ChannelKey;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BustardEmitter {
    private static final String INDENT = "    ";

    private final String implName;
    private final String pkgName;
    private final Class<? extends Bustard> supertype;

    private final Multimap<String, MethodDescription> listeners = HashMultimap.create();
    private final Map<String, String> executors = new HashMap<String, String>();

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
        listeners.put(ChannelKey.get(description.getEventGenericName(), description.getTopic()), description);
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
        for (MethodDescription description : listeners.values()) {
            emitIndent(writer, 2);
            writer.write(String.format("config.put(%s.class, %s, %s, %s, %b);\n",
                    description.getListenerName(),
                    stringLiteral(description.getEventGenericName()),
                    stringLiteral(description.getTopic()),
                    stringLiteral(description.getExecuteQualifierName()),
                    description.isEventOnBinding()));
        }
        emitIndent(writer, 1);
        writer.write("}\n\n");

        emitIndent(writer, 1);
        writer.write("@Override\n");
        emitIndent(writer, 1);
        writer.write("protected void post(Object subscriber, Object event, String key) throws Throwable {\n");
        for (String key : listeners.keySet()) {
            Collection<MethodDescription> descriptions = listeners.get(key);

            emitIndent(writer, 2);
            writer.write(String.format("if (\"%s\".equals(key)) {\n", key));

            boolean subscriberFirst = true;
            for (MethodDescription description : descriptions) {
                if (subscriberFirst) {
                    subscriberFirst = false;
                    emitIndent(writer, 3);
                } else {
                    writer.write(" else ");
                }
                writer.write(String.format("if (subscriber instanceof %s) {\n",
                        description.getListenerName()));

                emitIndent(writer, 4);
                writer.write(String.format("((%s) subscriber).%s((%s) event);\n",
                        description.getListenerName(),
                        description.getMethodName(),
                        description.getEventGenericName()));

                emitIndent(writer, 3);
                writer.write("}");
            }
            writer.write("\n");

            emitIndent(writer, 3);
            writer.write("return;\n");

            emitIndent(writer, 2);
            writer.write("}\n");
        }
        emitIndent(writer, 1);
        writer.write("}\n\n");

        writer.write("}");
    }

    private static void emitIndent(Writer writer, int level) throws IOException {
        for (int i = 0; i < level; i++) {
            writer.write(INDENT);
        }
    }

    private static String stringLiteral(String value) {
        return value == null ? String.valueOf((Object) null) : String.format("\"%s\"", value);
    }
}
