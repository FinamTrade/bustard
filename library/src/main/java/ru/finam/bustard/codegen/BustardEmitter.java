package ru.finam.bustard.codegen;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.io.Writer;

public class BustardEmitter {

    private Multimap<String, String[]> subscribers = HashMultimap.create();

    public void addSubscriber(String eventTypeName, String subscriberTypeName, String methodName) {
        subscribers.put(eventTypeName, new String[] { subscriberTypeName, methodName });
    }

    public void emit(Writer writer) throws IOException {
        writer.write("package ru.finam.bustard;\n\n");

        writer.write("import com.google.common.collect.Multimap;\n\n");

        writer.write("public class BustardImpl extends AbstractBustard {\n\n");

        writer.write("    @Override\n");
        writer.write("    void initialize(Multimap<String, String> eventTypes) {\n");
        for (String eventTypeName : subscribers.keySet()) {
            for (String[] data : subscribers.get(eventTypeName)) {
                String subscriberTypeName = data[0];

                writer.write(String.format("        eventTypes.put(\"class %s\", \"class %s\");\n",
                        subscriberTypeName,
                        eventTypeName));
            }
        }
        writer.write("    }\n\n");

        writer.write("    @Override\n");
        writer.write("    void post(Object listener, Object event) throws Throwable {\n");
        for (String eventTypeName : subscribers.keySet()) {
            writer.write(String.format("        if (event instanceof %s) {\n",
                    eventTypeName));

            for (String[] data : subscribers.get(eventTypeName)) {
                String subscriberTypeName = data[0];
                String methodName = data[1];

                writer.write(String.format("            if (listener instanceof %s) {\n",
                        subscriberTypeName));

                writer.write(String.format("                ((%s) listener).%s((%s) event);\n",
                        subscriberTypeName,
                        methodName,
                        eventTypeName));

                writer.write("            }\n");
            }
            writer.write("        }\n");
        }
        writer.write("    }\n\n");

        writer.write("}");
    }
}
