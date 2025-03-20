package com.merkle.oss.magnolia.content.indexer;

import info.magnolia.context.SystemContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.content.indexer.registry.IndexerDefinition;
import com.merkle.oss.magnolia.content.indexer.registry.IndexerDefinitionRegistry;

public class IndexerTrigger {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final java.util.function.Predicate<Node> NOT_ROOT_NODE = node -> !Objects.equals(Exceptions.wrap().get(node::getPath), "/");
    private final SystemContext systemContext;
    private final IndexerDefinitionRegistry indexerDefinitionRegistry;
    private final ComponentProvider componentProvider;

	@Inject
	public IndexerTrigger(
			final SystemContext systemContext,
			final IndexerDefinitionRegistry indexerDefinitionRegistry,
			final ComponentProvider componentProvider
	) {
        this.systemContext = systemContext;
        this.indexerDefinitionRegistry = indexerDefinitionRegistry;
        this.componentProvider = componentProvider;
	}

	public void index(final String indexerName, final String type, final String path, final boolean includeChildren) {
		final IndexerDefinition definition = indexerDefinitionRegistry.getProvider(indexerName).get();
		final LoggingIndexerWrapper indexer = new LoggingIndexerWrapper(componentProvider.getComponent(definition.getClazz()));
		LOG.info("Indexing {} under {} (includeChildren:{})...", definition.getName(), path, includeChildren);
		partition(getNodes(definition, type, path, includeChildren), definition.getBatchSize()).forEach(nodes -> indexer.index(nodes, type));
		LOG.info("Indexing {} under {} (includeChildren:{}) completed", definition.getName(), path, includeChildren);
	}

	public void remove(final String indexerName, final String type, final String path) {
		final IndexerDefinition definition = indexerDefinitionRegistry.getProvider(indexerName).get();
		final LoggingIndexerWrapper indexer = new LoggingIndexerWrapper(componentProvider.getComponent(definition.getClazz()));
		LOG.info("Removing {} under {}...", definition.getName(), path);
		partition(getNodes(definition, type, path, true).map(node ->
				new Indexer.IndexNode(
						NodeUtil.getNodeIdentifierIfPossible(node),
						NodeUtil.getNodePathIfPossible(node)
				)
		), definition.getBatchSize()).forEach(nodes -> indexer.remove(nodes, type));
		LOG.info("Removing {} under {} completed", definition.getName(), path);
	}

	private Stream<Node> getNodes(final IndexerDefinition definition, final String type, final String path, final boolean includeChildren) {
		return streamConfigs(definition, type).flatMap(config ->
				streamNodes(config, path, includeChildren).filter(NOT_ROOT_NODE.and(new AnyNodeTypesPredicate(config.nodeTypes())::evaluateTyped))
		);
	}

	private Stream<Config> streamConfigs(final IndexerDefinition definition, final String type) {
		return definition.getConfigs().stream().filter(config -> Objects.equals(type, config.type()));
	}

	private Stream<Node> streamNodes(final Config config, final String path, final boolean includeChildren) {
		final Node node = Exceptions.wrap().get(() -> systemContext.getJCRSession(config.workspace()).getNode(getValidatePath(config, path)));
		if(includeChildren) {
			return streamChildren(node);
		}
		return Stream.of(node);
	}

	private String getValidatePath(final Config config, final String path) {
		if (!path.startsWith(config.rootNodePath())) {
			throw new IllegalArgumentException("path " + path + " must be a child of " + config.rootNodePath());
		}
		return path;
	}

	private Stream<Node> streamChildren(final Node node) {
		final Provider<Iterator<Node>> iterator = () -> Exceptions.wrap().get(node::getNodes);
		return Stream.concat(
				Stream.of(node),
				StreamSupport
						.stream(Spliterators.spliteratorUnknownSize(iterator.get(), Spliterator.ORDERED),false)
						.flatMap(this::streamChildren)
		);
	}

	private <I> Stream<List<I>> partition(final Stream<I> source, final int batchSize) {
		return StreamSupport.stream(Iterables.partition(source::iterator, batchSize).spliterator(), source.isParallel());
	}
}
