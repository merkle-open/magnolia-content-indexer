package com.merkle.oss.magnolia.content.indexer.registry;

import static java.util.function.Predicate.not;

import info.magnolia.config.registry.DefinitionMetadata;
import info.magnolia.config.registry.Registry;
import info.magnolia.config.registry.decoration.DefinitionDecorator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.Node;

import com.merkle.oss.magnolia.builder.AbstractDynamicDefinitionProvider;
import com.merkle.oss.magnolia.builder.DynamicDefinitionMetaData;
import com.merkle.oss.magnolia.content.indexer.Config;
import com.merkle.oss.magnolia.content.indexer.Indexer;
import com.merkle.oss.magnolia.content.indexer.annotation.IndexerFactory;
import com.merkle.oss.magnolia.content.indexer.annotation.NodePredicate;

public class IndexerDefinitionProvider extends AbstractDynamicDefinitionProvider<IndexerDefinition> {
    private final IndexerFactory annotation;
    private final DefinitionMetadata metadata;
    private final ComponentProvider componentProvider;
    private final Class<? extends Indexer> factoryClass;

    public IndexerDefinitionProvider(
            final ComponentProvider componentProvider,
            final List<DefinitionDecorator<IndexerDefinition>> decorators,
            final Class<? extends Indexer> factoryClass
    ) {
        super(decorators);
        this.componentProvider = componentProvider;
        this.factoryClass = factoryClass;
        this.annotation = factoryClass.getDeclaredAnnotation(IndexerFactory.class);
        this.metadata = new IndexerDefinitionMetaDataBuilder(factoryClass, annotation.id())
                .type(IndexerDefinitionRegistry.TYPE)
                .build();
    }

    @Override
    public DefinitionMetadata getMetadata() {
        return metadata;
    }

    @Override
    public IndexerDefinition getInternal() throws Registry.InvalidDefinitionException {
        return new IndexerDefinition(
                annotation.name(),
                factoryClass,
                annotation.batchSize(),
                Arrays.stream(annotation.configs()).map(this::create).collect(Collectors.toSet())
        );
    }

    private Config create(final IndexerFactory.Config annotation) {
        final NodePredicate nodePredicate = Optional.of(annotation.predicate())
                .filter(not(NodePredicate.class::equals))
                .map(predicateClass -> (NodePredicate)componentProvider.newInstance(predicateClass))
                .orElseGet(() -> ignored -> true);

        return new Config(
                annotation.type(),
                Duration.ofMillis(annotation.delayInMs()),
                annotation.workspace(),
                annotation.rootNode(),
                new ExceptionHandlingPredicate<>(
                        nodePredicate.and(new AnyNodeTypesPredicate(Set.of(annotation.nodeTypes()))::evaluateTyped),
                        false
                )
        );
    }

    private static class IndexerDefinitionMetaDataBuilder extends DynamicDefinitionMetaData.Builder {
        public IndexerDefinitionMetaDataBuilder(final Class<?> factoryClass, final String id) {
            super(factoryClass, id);
        }
        @Override
        protected String buildReferenceId() {
            return getName();
        }
    }

    public static class Factory {
        public IndexerDefinitionProvider create(final Class<? extends Indexer> factoryClass) {
            return new IndexerDefinitionProvider(
                    Components.getComponentProvider(),
                    Collections.emptyList(),
                    factoryClass
            );
        }
    }
}
