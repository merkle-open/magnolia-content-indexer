package com.merkle.oss.magnolia.content.indexer;

import java.time.Duration;
import java.util.function.Predicate;

import javax.jcr.Node;

public record Config(
        String type,
        Duration delay,
        String workspace,
        String rootNodePath,
        Predicate<Node> predicate
) {}
