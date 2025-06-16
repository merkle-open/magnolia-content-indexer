package com.merkle.oss.magnolia.content.indexer;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.content.indexer.registry.IndexerDefinition;

public class DataListener implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SystemContext systemContext;
    private final LoggingIndexerWrapper indexer;
    private final IndexerDefinition definition;
    private final Config config;
    private final EventFilter eventFilter;

    public DataListener(
            final SystemContext systemContext,
            final Indexer indexer,
            final IndexerDefinition definition,
            final Config config,
            final EventFilter eventFilter
    ) {
        this.systemContext = systemContext;
        this.indexer = new LoggingIndexerWrapper(indexer);
        this.definition = definition;
        this.config = config;
        this.eventFilter = eventFilter;
    }

    @Override
    public void onEvent(final EventIterator events) {
        try {
            MgnlContext.setInstance(systemContext);
            final Set<Event> filteredEvents = eventFilter.getFilteredEvents(events);
            partition(getNodes(filteredEvents, EventFilter.REMOVE_OR_MOVE_NODE_EVENT_PREDICATE), definition.getBatchSize()).forEach(this::remove);
            partition(getNodes(filteredEvents, EventFilter.REMOVE_NODE_EVENT_PREDICATE.negate()), definition.getBatchSize()).forEach(this::index);
        } finally {
            systemContext.release();
        }
    }

    private void index(final Collection<Indexer.IndexNode> indexNodes) {
        try {
            LOG.debug("Indexing nodes {}...", indexNodes);
            final Session session = systemContext.getJCRSession(config.workspace());
            final Set<Node> nodes = indexNodes.stream()
                    .flatMap(indexNode -> getNode(session, indexNode).stream())
                    .filter(node -> new AnyNodeTypesPredicate(config.nodeTypes()).evaluateTyped(node))
                    .collect(Collectors.toSet());
            if (!nodes.isEmpty()) {
                indexer.index(nodes, Collections.emptyMap(), config.type());
            }
        } catch (Exception e) {
            LOG.error("Failed to index nodes {}", indexNodes, e);
        }
    }

    private Optional<Node> getNode(final Session session, final Indexer.IndexNode indexNode) {
        try {
            return Optional.of(session.getNodeByIdentifier(indexNode.identifier()));
        } catch (RepositoryException e) {
            LOG.warn("Failed to get node {} workspace {}", indexNode, config.workspace());
            return Optional.empty();
        }
    }

    private void remove(final Collection<Indexer.IndexNode> indexNodes) {
        try {
            LOG.debug("Removing nodes {}...", indexNodes);
            if (!indexNodes.isEmpty()) {
                indexer.remove(indexNodes, Collections.emptyMap(), config.type());
            }
        } catch (Exception e) {
            LOG.error("Failed to remove nodes {}", indexNodes, e);
        }
    }

    private Stream<Indexer.IndexNode> getNodes(final Collection<Event> eventSet, final Predicate<Event> filter) {
        return eventSet.stream()
                .filter(filter)
                .filter(event -> Exceptions.wrap().get(event::getPath) != null)
                .map(event -> Exceptions.wrap().get(() ->
                        new Indexer.IndexNode(event.getIdentifier(), event.getPath())
                ))
                .filter(distinctByKey(Indexer.IndexNode::identifier))
                .distinct();
    }

    private <I> Stream<List<I>> partition(final Stream<I> source, final int batchSize) {
        return StreamSupport.stream(Iterables.partition(source::iterator, batchSize).spliterator(), source.isParallel());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        final Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
