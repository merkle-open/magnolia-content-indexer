package com.merkle.oss.magnolia.content.indexer;

import info.magnolia.jcr.util.NodeUtil;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSingleIndexer implements Indexer {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void index(final Collection<Node> nodes, final String type) {
        for (final Node node : nodes) {
            final String identifier = NodeUtil.getNodeIdentifierIfPossible(node);
            try {
                LOG.debug("Indexing " + identifier + " of type " + type + "...");
                index(node, type);
            } catch (Exception e){
                LOG.error("Failed to index " + identifier + "!", e);
            }
        }
    }
    protected abstract void index(Node node, String type) throws Exception;

    @Override
    public void remove(final Collection<IndexNode> nodes, final String type) {
        for (final IndexNode node : nodes) {
            try {
                LOG.debug("Removing " + node + " of type " + type + "...");
                remove(node, type);
            } catch (Exception e){
                LOG.error("Failed to remove " + node + "!", e);
            }
        }
    }
    protected abstract void remove(IndexNode identifier, String type)  throws Exception;
}
