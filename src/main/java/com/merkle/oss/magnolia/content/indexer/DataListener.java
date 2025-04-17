package com.merkle.oss.magnolia.content.indexer;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
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
import com.google.common.collect.Sets;
import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.content.indexer.registry.IndexerDefinition;

public class DataListener implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Predicate<Event> REMOVE_NODE_EVENT_PREDICATE = event -> event.getType() == Event.NODE_REMOVED;
    private static final Predicate<Event> ADD_NODE_EVENT_PREDICATE = event -> event.getType() == Event.NODE_ADDED;
    private final SystemContext systemContext;
    private final LoggingIndexerWrapper indexer;
    private final IndexerDefinition definition;
    private final Config config;

    public DataListener(
            final SystemContext systemContext,
            final Indexer indexer,
            final IndexerDefinition definition,
            final Config config
    ) {
        this.systemContext = systemContext;
        this.indexer = new LoggingIndexerWrapper(indexer);
        this.definition = definition;
        this.config = config;
    }

    @Override
    public void onEvent(final EventIterator events) {
        final Map<String, Set<Event>> eventSet = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize((Iterator<Event>) events, Spliterator.ORDERED), false)
                .filter(event -> Exceptions.wrap().get(event::getIdentifier) != null)
                .collect(Collectors.toMap(event -> Exceptions.wrap().get(event::getIdentifier), Set::of, Sets::union));
        LOG.debug("handling events: " + eventSet);
        try {
            MgnlContext.setInstance(systemContext);
            final Set<Event> filteredEvents = getFilteredEvents(eventSet);
            partition(getNodes(filteredEvents, REMOVE_NODE_EVENT_PREDICATE), definition.getBatchSize()).forEach(this::remove);
            partition(getNodes(filteredEvents, REMOVE_NODE_EVENT_PREDICATE.negate()), definition.getBatchSize()).forEach(this::index);
        } finally {
            systemContext.release();
        }
    }

    private Set<Event> getFilteredEvents(final Map<String, Set<Event>> events) {
        return events.values().stream()
                .flatMap(values -> {
                    final boolean nodeAdded = values.stream().anyMatch(ADD_NODE_EVENT_PREDICATE);
                    final boolean nodeRemoved = values.stream().anyMatch(REMOVE_NODE_EVENT_PREDICATE);
                    if(nodeAdded && nodeRemoved) {
                        return Stream.empty();
                    }
                    if(nodeRemoved) {
                         return values.stream().filter(REMOVE_NODE_EVENT_PREDICATE);
                    }
                    return values.stream();
                })
                .collect(Collectors.toSet());
    }

    private void index(final Collection<Indexer.IndexNode> indexNodes) {
        try {
            LOG.debug("Indexing nodes {}...", indexNodes);
            final Session session = systemContext.getJCRSession(config.workspace());
            final Set<Node> nodes = indexNodes.stream()
                    .flatMap(indexNode -> getNode(session, indexNode).stream())
                    .filter(node -> new AnyNodeTypesPredicate(config.nodeTypes()).evaluateTyped(node))
                    .collect(Collectors.toSet());
            if(!nodes.isEmpty()) {
                indexer.index(nodes, Collections.emptyMap(), config.type());
            }
        } catch (Exception e) {
            LOG.error("Failed to index nodes " + indexNodes, e);
        }
    }

    private Optional<Node> getNode(final Session session, final Indexer.IndexNode indexNode) {
        try {
            return Optional.of(session.getNodeByIdentifier(indexNode.identifier()));
        } catch (RepositoryException e) {
            LOG.warn("Failed to get node " + indexNode + " workspace " + config.workspace());
            return Optional.empty();
        }
    }

    private void remove(final Collection<Indexer.IndexNode> indexNodes) {
        try {
            LOG.debug("Removing nodes {}...", indexNodes);
            if(!indexNodes.isEmpty()) {
                indexer.remove(indexNodes, Collections.emptyMap(), config.type());
            }
        } catch (Exception e) {
            LOG.error("Failed to remove nodes " + indexNodes, e);
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
