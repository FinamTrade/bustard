package ru.finam.bustard.codegen;

import junit.framework.Assert;
import org.junit.Test;
import ru.finam.bustard.AbstractBustard;

import java.io.IOException;
import java.io.StringWriter;

public class TestBustardEmitter {

    @Test
    public void repeatSubscriber() throws IOException {
        StringWriter writer = new StringWriter();

        BustardEmitter emitter = new BustardEmitter(
                BustardGenerator.PACKAGE_NAME,
                BustardGenerator.IMPL_NAME,
                AbstractBustard.class);

        emitter.addSubscriber("SomeEvent", "Subscriber", "listen", null);
        emitter.addSubscriber("SomeEvent", "Subscriber", "listen", null);

        emitter.emit(writer);

        String result =
                "package ru.finam.bustard;\n" +
                "\n" +
                "public class BustardImpl extends ru.finam.bustard.AbstractBustard {\n" +
                "\n" +
                "    @Override\n" +
                "    protected void initialize(Config config) {\n" +
                "        config.put(Subscriber.class, SomeEvent.class);\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    protected void post(Object subscriber, Object event) throws Throwable {\n" +
                "        if (event instanceof SomeEvent) {\n" +
                "            if (subscriber instanceof Subscriber) {\n" +
                "                ((Subscriber) subscriber).listen((SomeEvent) event);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "}";

        Assert.assertEquals(result, writer.getBuffer().toString());
    }
}
