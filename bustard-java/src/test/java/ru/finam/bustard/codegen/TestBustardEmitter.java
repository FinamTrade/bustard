package ru.finam.bustard.codegen;

import org.junit.Assert;
import org.junit.Test;
import ru.finam.bustard.ChannelKey;
import ru.finam.bustard.java.AbstractJavaBustard;

import java.io.IOException;
import java.io.StringWriter;

public class TestBustardEmitter {

    @Test
    public void repeatSubscriber() throws IOException {
        StringWriter writer = new StringWriter();

        BustardEmitter emitter = new BustardEmitter(
                BustardGenerator.BUSTARD_JAVA_PACKAGE_NAME,
                BustardGenerator.BUSTARD_IMPL_NAME,
                AbstractJavaBustard.class);

        emitter.addSubscriber(new MethodDescription("Subscriber", "listen", "SomeEvent", null, false, ""));
        emitter.addSubscriber(new MethodDescription("Subscriber", "listen", "SomeEvent", null, false, ""));

        emitter.emit(writer);

        String result =
                "package ru.finam.bustard.java;\n" +
                "\n" +
                "public class BustardImpl extends ru.finam.bustard.java.AbstractJavaBustard {\n" +
                "\n" +
                "    @Override\n" +
                "    protected void initialize(ru.finam.bustard.Config config) {\n" +
                "        config.put(Subscriber.class, \"SomeEvent\", \"\", null, false);\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    protected void post(Object subscriber, Object event, String key) throws Throwable {\n" +
                "        if (\"" + ChannelKey.get("SomeEvent") + "\".equals(key)) {\n" +
                "            if (subscriber instanceof Subscriber) {\n" +
                "                ((Subscriber) subscriber).listen((SomeEvent) event);\n" +
                "            }\n" +
                "            return;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "}";

        Assert.assertEquals(result, writer.getBuffer().toString());
    }
}
