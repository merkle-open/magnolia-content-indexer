package com.merkle.oss.magnolia.content.indexer;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinezoo.noexception.Exceptions;

public class EventFilter {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static final Predicate<Event> ADD_NODE_EVENT_PREDICATE = event -> event.getType() == Event.NODE_ADDED;
    static final Predicate<Event> REMOVE_NODE_EVENT_PREDICATE = event -> event.getType() == Event.NODE_REMOVED;
    static final Predicate<Event> MOVE_NODE_EVENT_PREDICATE = event -> event.getType() == Event.NODE_MOVED;
    static final Predicate<Event> REMOVE_OR_MOVE_NODE_EVENT_PREDICATE = EventFilter.REMOVE_NODE_EVENT_PREDICATE.or(EventFilter.MOVE_NODE_EVENT_PREDICATE);

    public Set<Event> getFilteredEvents(final EventIterator events) {
        final Map<String, List<Event>> eventSet = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize((Iterator<Event>) events, Spliterator.ORDERED), false)
                .filter(event -> Exceptions.wrap().get(event::getIdentifier) != null)
                .collect(Collectors.groupingBy(event -> Exceptions.wrap().get(event::getIdentifier)));
        LOG.debug("handling events: {}", events);
        return getFilteredEvents(eventSet);
    }

    private Set<Event> getFilteredEvents(final Map<String, List<Event>> events) {
        return events.values().stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(values -> {
                    final long nodesAdded = values.stream().filter(ADD_NODE_EVENT_PREDICATE).count();
                    final long nodesRemoved = values.stream().filter(REMOVE_NODE_EVENT_PREDICATE).count();
                    final long nodesMoved = values.stream().filter(MOVE_NODE_EVENT_PREDICATE).count();

                    // Optional move actions followed by a delete action -> remove event
                    if (nodesRemoved > nodesAdded) {
                        return values.stream().filter(REMOVE_NODE_EVENT_PREDICATE);
                    }

                    // At least one move action of an existing node (not followed by a delete action) -> move event
                    if (nodesMoved > 0 && nodesRemoved <= nodesMoved && nodesAdded <= nodesMoved) {
                        return values.stream().filter(MOVE_NODE_EVENT_PREDICATE);
                    }

                    // Add action, optionally followed by move actions, ending with a delete action -> ignore
                    if (nodesRemoved > 0 && nodesRemoved == nodesAdded) {
                        return Stream.empty();
                    }

                    // Add or edit action -> add or change event
                    return values.stream().filter(REMOVE_OR_MOVE_NODE_EVENT_PREDICATE.negate());
                })
                .collect(Collectors.toSet());
    }
}
