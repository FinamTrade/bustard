package ru.finam.bustard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
public @interface ExecuteQualifier {
    Class<? extends Executor> value();
}
