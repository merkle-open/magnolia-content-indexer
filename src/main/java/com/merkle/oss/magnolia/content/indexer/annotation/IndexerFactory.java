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
    int batchSize() default Integer.MAX_VALUE;
    Config[] configs();
    Class<? extends EventPredicate> filter() default EventPredicate.SystemExcludingPredicates.class;

    @interface Config {
        String type();
        int delayInMs() default 5000;
        String workspace();
        String rootNode() default "/";
        String[] nodeTypes() default {};
        Class<? extends EventPredicate> filter() default EventPredicate.class;
        Class<? extends NodePredicate> predicate() default NodePredicate.class;
    }
}
