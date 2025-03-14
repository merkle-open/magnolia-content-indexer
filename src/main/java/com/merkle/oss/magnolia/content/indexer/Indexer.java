package com.merkle.oss.magnolia.content.indexer;

import javax.jcr.Node;

public interface Indexer {
	/**
	 * Indexes data from a given node to the index
	 *
	 * @param node - node to index
	 * @param type - config type
	 */
	void index(Node node, String type) throws Exception;

	/**
	 * Removes all entries under a given path from the index<br>
	 * (nodes can't be passed, since they are already deleted)
	 *
	 * @param path - all entries indexed starting with this path should be removed
	 * @param type - config type
	 */
	void remove(String path, String type) throws Exception;
}
