package com.merkle.oss.magnolia.content.indexer;

import info.magnolia.jcr.util.NodeUtil;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingIndexerWrapper implements Indexer {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Indexer wrapped;

    public LoggingIndexerWrapper(final Indexer wrapped) {
        this.wrapped = wrapped;
    }

    public void index(final Collection<Node> nodes, final String type) {
        final List<String> nodeIdentifiers = nodes.stream().map(NodeUtil::getNodeIdentifierIfPossible).toList();
        try {
            LOG.debug("Indexing " + nodeIdentifiers + " of type " + type + "...");
            wrapped.index(nodes, type);
        } catch (Exception e) {
            LOG.error("Failed to index " + nodeIdentifiers + "!", e);
        }
    }

    public void remove(final Collection<IndexNode> nodes, final String type) {
        try {
            LOG.debug("Removing " + nodes + "of type " + type + "...");
            wrapped.remove(nodes, type);
        } catch (Exception e) {
            LOG.error("Failed to remove " + nodes + "!", e);
        }
    }
}
