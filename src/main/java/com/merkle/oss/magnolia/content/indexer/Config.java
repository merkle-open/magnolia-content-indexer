package com.merkle.oss.magnolia.content.indexer;

import java.time.Duration;
import java.util.Set;

public record Config(
        String type,
        Duration delay,
        String workspace,
        String rootNodePath,
        Set<String> nodeTypes
) {}
