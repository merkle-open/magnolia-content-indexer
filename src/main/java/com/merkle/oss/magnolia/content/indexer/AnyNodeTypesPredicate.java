package com.merkle.oss.magnolia.content.indexer;

import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.predicate.NodeTypePredicate;

import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.Node;

public class AnyNodeTypesPredicate extends AbstractPredicate<Node> {
    private final Set<AbstractPredicate<Node>> predicates;

    public AnyNodeTypesPredicate(final Set<String> nodeTypes) {
        this.predicates = nodeTypes.stream()
                .map(NodeTypePredicate::new)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean evaluateTyped(final Node node) {
        return predicates.isEmpty() || predicates.stream().anyMatch(predicate -> predicate.evaluateTyped(node));
    }
}
