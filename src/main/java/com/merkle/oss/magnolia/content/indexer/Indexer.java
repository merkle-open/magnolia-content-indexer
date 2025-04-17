package com.merkle.oss.magnolia.content.indexer;

import java.util.Collection;
import java.util.Map;

import javax.jcr.Node;

public interface Indexer {
	/**
	 * Indexes data from a given node to the index
	 *
	 * @param nodes - nodes to index
	 * @param indexTriggerParams - optional parameters that can be passed to the indexer from the {@link IndexerTrigger} (will be empty when triggered from jcr observer)
	 * @param type - config type
	 */
	void index(Collection<Node> nodes, Map<String, Object> indexTriggerParams, String type) throws Exception;

	/**
	 * Removes data with a given identifier from the index<br>
	 * (nodes can't be passed, since they are already deleted)
	 *
	 * @param nodes - removed nodes
	 * @param indexTriggerParams - optional parameters that can be passed to the indexer from the {@link IndexerTrigger} (will be empty when triggered from jcr observer)
	 * @param type - config type
	 */
	void remove(Collection<IndexNode> nodes, Map<String, Object> indexTriggerParams, String type) throws Exception;

	record IndexNode(String identifier, String path){}
}
