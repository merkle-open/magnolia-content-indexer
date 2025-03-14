package com.merkle.oss.magnolia.content.indexer.registry;

import info.magnolia.config.registry.DefinitionMetadata;
import info.magnolia.config.registry.Registry;
import info.magnolia.config.registry.decoration.DefinitionDecorator;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.merkle.oss.magnolia.builder.AbstractDynamicDefinitionProvider;
import com.merkle.oss.magnolia.builder.DynamicDefinitionMetaData;
import com.merkle.oss.magnolia.content.indexer.Config;
import com.merkle.oss.magnolia.content.indexer.annotation.IndexerFactory;

public class IndexerDefinitionProvider extends AbstractDynamicDefinitionProvider<IndexerDefinition> {
    private final IndexerFactory annotation;
    private final DefinitionMetadata metadata;
    private final Class<? extends com.merkle.oss.magnolia.content.indexer.Indexer> factoryClass;

    public IndexerDefinitionProvider(
            final List<DefinitionDecorator<IndexerDefinition>> decorators,
            final Class<? extends com.merkle.oss.magnolia.content.indexer.Indexer> factoryClass
    ) {
        super(decorators);
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
                Arrays.stream(annotation.configs()).map(this::create).collect(Collectors.toSet())
        );
    }

    private Config create(final IndexerFactory.Config annotation) {
        return new Config(
                annotation.type(),
                Duration.ofMillis(annotation.delayInMs()),
                annotation.workspace(),
                annotation.rootNode(),
                Set.of(annotation.nodeTypes())
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
        public IndexerDefinitionProvider create(final Class<? extends com.merkle.oss.magnolia.content.indexer.Indexer> factoryClass) {
            return new IndexerDefinitionProvider(
                    Collections.emptyList(),
                    factoryClass
            );
        }
    }
}
