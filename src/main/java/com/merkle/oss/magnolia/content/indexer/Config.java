package com.merkle.oss.magnolia.content.indexer;

import java.time.Duration;
import java.util.function.Predicate;

import javax.jcr.Node;
import javax.jcr.observation.Event;

import jakarta.annotation.Nullable;

public record Config(
        String type,
        Duration delay,
        String workspace,
        String rootNodePath,
        @Nullable Predicate<Event> filter,
        Predicate<Node> predicate
) {}
