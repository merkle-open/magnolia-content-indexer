package com.merkle.oss.magnolia.content.indexer.registry;

import java.lang.invoke.MethodHandles;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandlingPredicate<T> implements Predicate<T> {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Predicate<T> wrapped;
    private final boolean fallbackOnError;

    public ExceptionHandlingPredicate(final Predicate<T> wrapped, final boolean fallbackOnError) {
        this.wrapped = wrapped;
        this.fallbackOnError = fallbackOnError;
    }

    @Override
    public boolean test(final T t) {
        try {
            return wrapped.test(t);
        } catch (Exception e) {
            LOG.error("Failed to evaluate predicate "+wrapped.getClass()+"! returning fallback "+fallbackOnError, e);
            return fallbackOnError;
        }
    }
}
