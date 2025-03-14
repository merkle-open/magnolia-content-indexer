package com.merkle.oss.magnolia.content.indexer.registry;

import info.magnolia.config.registry.DefinitionProvider;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.merkle.oss.magnolia.builder.AbstractDynamicConfigurationSource;
import com.merkle.oss.magnolia.content.indexer.Indexer;
import com.merkle.oss.magnolia.content.indexer.annotation.IndexerFactories;

public class IndexerDefinitionConfigurationSource extends AbstractDynamicConfigurationSource<IndexerDefinition> {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final IndexerDefinitionProvider.Factory indexerDefinitionProviderFactory;

    @Inject
    public IndexerDefinitionConfigurationSource(
            @IndexerFactories final Set<Class<?>> indexerFactories,
            final IndexerDefinitionProvider.Factory indexerDefinitionProviderFactory
    ) {
        super(IndexerDefinitionRegistry.TYPE, indexerFactories);
        this.indexerDefinitionProviderFactory = indexerDefinitionProviderFactory;
    }

    @Override
    protected Stream<DefinitionProvider<IndexerDefinition>> definitionProviders(final Class<?> factoryClass) {
        LOG.info("Registered indexer '{}' from {}", factoryClass.getSimpleName(), factoryClass.getName());
        return Stream.of(indexerDefinitionProviderFactory.create((Class<? extends Indexer>) factoryClass));
    }
}
