package com.merkle.oss.magnolia.content.indexer;

import info.magnolia.jcr.util.NodeUtil;

import java.lang.invoke.MethodHandles;

import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingIndexerWrapper implements Indexer {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Indexer wrapped;

    public LoggingIndexerWrapper(final Indexer wrapped) {
        this.wrapped = wrapped;
    }

    public void index(final Node node, final String type) {
        final String path = NodeUtil.getPathIfPossible(node);
        try {
            LOG.debug("Indexing " + path + " of type " + type + "...");
            wrapped.index(node, type);
        } catch (Exception e) {
            LOG.error("Failed to index " + path + "!", e);
        }
    }

    public void remove(final String path, final String type) {
        try {
            LOG.debug("Removing " + path + "of type " + type + "...");
            wrapped.remove(path, type);
        } catch (Exception e) {
            LOG.error("Failed to remove " + path + "!", e);
        }
    }
}
