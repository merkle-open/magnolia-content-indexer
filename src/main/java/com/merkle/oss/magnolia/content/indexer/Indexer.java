package com.merkle.oss.magnolia.content.indexer;

import java.util.Collection;

import javax.jcr.Node;

public interface Indexer {
	/**
	 * Indexes data from a given node to the index
	 *
	 * @param nodes - nodes to index
	 * @param type - config type
	 */
	void index(Collection<Node> nodes, String type) throws Exception;

	/**
	 * Removes data with a given identifier from the index<br>
	 * (nodes can't be passed, since they are already deleted)
	 *
	 * @param nodes - removed nodes
	 * @param type - config type
	 */
	void remove(Collection<IndexNode> nodes, String type) throws Exception;

	record IndexNode(String identifier, String path){}
}
