package com.merkle.oss.magnolia.content.indexer.annotation;

import info.magnolia.jcr.util.NodeTypes;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.jcr.observation.Event;

import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface EventPredicate extends Predicate<Event> {

    // See info.magnolia.cms.util.FilteredEventListener
    class SystemExcludingPredicates implements EventPredicate {
        private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
        private static final Set<String> BLACKLIST_PROPERTY_PREFIX = Set.of(
                NodeTypes.JCR_PREFIX,
                NodeTypes.MGNL_PREFIX
        );
        private static final Pattern BLACKLIST_PATTERN = Pattern.compile("/("+ String.join("|", BLACKLIST_PROPERTY_PREFIX) +")[^/]+$");
        @Override
        public boolean test(final Event event) {
            try {
                final String path = event.getPath();
                return !path.startsWith("/"+ JcrConstants.JCR_SYSTEM) && !BLACKLIST_PATTERN.matcher(path).find();
            } catch (Exception e) {
                LOG.error("Could not get path of event {}.", event);
                return false;
            }
        }
    }
}
