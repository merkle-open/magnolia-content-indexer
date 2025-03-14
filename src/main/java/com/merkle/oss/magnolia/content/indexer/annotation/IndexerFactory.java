package com.merkle.oss.magnolia.content.indexer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface IndexerFactory {
    String id();
    String name();
    Config[] configs();

    @interface Config {
        String type();
        int delayInMs() default 5000;
        String workspace();
        String rootNode() default "/";
        String[] nodeTypes() default {};
    }
}
