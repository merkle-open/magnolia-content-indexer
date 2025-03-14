package com.merkle.oss.magnolia.content.indexer;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.merkle.oss.magnolia.content.indexer.registry.IndexerDefinitionConfigurationSource;
import com.merkle.oss.magnolia.content.indexer.registry.IndexerDefinitionRegistry;

public class ContentIndexerModule implements ModuleLifecycle {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final IndexerDefinitionRegistry indexerDefinitionRegistry;
    private final IndexerDefinitionConfigurationSource indexerDefinitionConfigurationSource;
    private final DataListenerRegistrar dataListenerRegistrar;

    @Inject
	public ContentIndexerModule(
			final IndexerDefinitionRegistry indexerDefinitionRegistry,
			final IndexerDefinitionConfigurationSource indexerDefinitionConfigurationSource,
			final DataListenerRegistrar dataListenerRegistrar
	) {
        this.indexerDefinitionRegistry = indexerDefinitionRegistry;
        this.indexerDefinitionConfigurationSource = indexerDefinitionConfigurationSource;
        this.dataListenerRegistrar = dataListenerRegistrar;
    }

	@Override
	public void start(final ModuleLifecycleContext moduleLifecycleContext) {
		LOG.debug("Starting content indexer module");
		indexerDefinitionRegistry.bindTo(indexerDefinitionConfigurationSource);
		indexerDefinitionConfigurationSource.start();
		dataListenerRegistrar.register();
	}

	@Override
	public void stop(final ModuleLifecycleContext moduleLifecycleContext) {
		LOG.debug("Stopping content indexer module");
		dataListenerRegistrar.unregister();
	}
}
