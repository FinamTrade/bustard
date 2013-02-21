package ru.finam.bustard.codegen;

import junit.framework.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class TestLinePattern {
    private static Pattern pattern = ListenersFinder.LINE_PATTERN;

    @Test
    public void test() {
        Assert.assertEquals(pattern.matcher("ru.finam.Sample listenMethod ru.finam.Event null true Topic").matches(), true);
        Assert.assertEquals(pattern.matcher("5ru.finam.Sample listenMethod ru.finam.Event null false Topic").matches(), false);
        Assert.assertEquals(pattern.matcher("ru.finam.Sample 5listenMethod ru.finam.Event null true Topic").matches(), false);
        Assert.assertEquals(pattern.matcher("ru.finam.Sample123 listenMethod123 ru.finam.Event123 null false Topic").matches(), true);
        Assert.assertEquals(pattern.matcher("Sample listenMethod123 ru.finam.Event123 null true Topic").matches(), true);
        Assert.assertEquals(pattern.matcher("ru.finam.Sample ru.finam.Sample.listenMethod ru.finam.Event null false Topic").matches(), false);
        Assert.assertEquals(pattern.matcher("ru.finam.Sample listenMethod null false Topic").matches(), false);
        Assert.assertEquals(pattern.matcher("ru.finam.Sample listenMethod ru.finam.Event ru.finam.Event null false Topic").matches(), false);
        Assert.assertEquals(pattern.matcher("ru.finam.Sample listenMethod ru.finam.Event ru.finam.SomeQualifier false Topic").matches(), true);
        Assert.assertEquals(pattern.matcher("ru.finam.Sample listenMethod ru.finam.Event ru.finam.SomeQualifier true Topic").matches(), true);
        Assert.assertEquals(pattern.matcher("ru.finam.Sample listenMethod ru.finam.Event ru.finam.SomeQualifier Topic").matches(), false);
        Assert.assertEquals(pattern.matcher("ru.finam.Sample listenMethod ru.finam.Event ru.finam.SomeQualifier null Topic").matches(), false);
        Assert.assertEquals(pattern.matcher("ru.finam.Sample listenMethod ru.finam.Event ru.finam.SomeQualifier true Topic").matches(), true);
        Assert.assertEquals(pattern.matcher("ru.finam.Sample listenMethod ru.finam.Event ru.finam.SomeQualifier true").matches(), false);
        Assert.assertEquals(pattern.matcher("ru.finam.Sample listenMethod ru.finam.Event ru.finam.SomeQualifier true ").matches(), true);
    }
}
