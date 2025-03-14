package com.merkle.oss.magnolia.content.indexer;

import info.magnolia.context.SystemContext;

import java.lang.invoke.MethodHandles;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinezoo.noexception.Exceptions;

public class DataListener implements EventListener {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SystemContext systemContext;
    private final LoggingIndexerWrapper indexer;
    private final Config config;

	public DataListener(
		final SystemContext systemContext,
		final Indexer indexer,
		final Config config
	) {
        this.systemContext = systemContext;
        this.indexer = new LoggingIndexerWrapper(indexer);
        this.config = config;
	}

	@Override
	public void onEvent(final EventIterator events) {
		StreamSupport
				.stream(Spliterators.spliteratorUnknownSize((Iterator<Event>) events, Spliterator.ORDERED),false)
				.filter(event -> Exceptions.wrap().get(event::getIdentifier) != null)
				.sorted(Comparator.comparing(event -> event.getType() == Event.NODE_REMOVED))
				.filter(distinctByKey(event -> Exceptions.wrap().get(event::getIdentifier)))
				.forEach(this::handleSafe);
	}

	private void handleSafe(final Event event) {
		try {
			LOG.debug("handling node change event {}...", event);
			handle(event);
		} catch (Exception e) {
			LOG.error("Failed to handle node change event " + event, e);
		}
	}

	private void handle(final Event event) throws Exception {
		final int eventType = event.getType();
		if (eventType == Event.NODE_REMOVED) {
			indexer.remove(event.getPath(), config.type());
		} else {
			try {
				final Node node = systemContext.getJCRSession(config.workspace()).getNodeByIdentifier(event.getIdentifier());
				indexer.index(node, config.type());
			} catch (ItemNotFoundException ignored) {}
		}
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		final Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}
}
