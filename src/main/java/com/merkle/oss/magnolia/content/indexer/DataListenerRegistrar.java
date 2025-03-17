package com.merkle.oss.magnolia.content.indexer;


import info.magnolia.cms.util.FilteredEventListener;
import info.magnolia.context.SystemContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.observation.WorkspaceEventListenerRegistration;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.commons.predicate.Predicate;

import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.content.indexer.registry.IndexerDefinition;
import com.merkle.oss.magnolia.content.indexer.registry.IndexerDefinitionRegistry;

@Singleton
public class DataListenerRegistrar {
    private static final Predicate FILTER = FilteredEventListener.JCR_SYSTEM_EXCLUDING_PREDICATE;
    private final IndexerDefinitionRegistry indexerDefinitionRegistry;
    private final SystemContext systemContext;
    private final ComponentProvider componentProvider;
    private final Set<WorkspaceEventListenerRegistration.Handle> registrations = new HashSet<>();

    @Inject
    public DataListenerRegistrar(
            final IndexerDefinitionRegistry indexerDefinitionRegistry,
            final SystemContext systemContext,
            final ComponentProvider componentProvider
    ) {
        this.indexerDefinitionRegistry = indexerDefinitionRegistry;
        this.systemContext = systemContext;
        this.componentProvider = componentProvider;
    }

    public void register() {
        indexerDefinitionRegistry.getAllDefinitions().forEach(this::register);
    }

    private void register(final IndexerDefinition definition) {
        final Indexer indexer = componentProvider.getComponent(definition.getClazz());
        definition.getConfigs().forEach(config ->
            Exceptions.wrap().run(() -> register(indexer, definition, config))
        );
    }

    private void register(final Indexer indexer, final IndexerDefinition definition, final Config config) throws RepositoryException {
        final DataListener eventListener = new DataListener(systemContext, indexer, definition, config);
        registrations.add(
                WorkspaceEventListenerRegistration
                        .observe(config.workspace(), config.rootNodePath(), new FilteredEventListener(eventListener, FILTER))
                        .withDelay(config.delay().toMillis())
                        .withNodeTypes(config.nodeTypes().isEmpty() ? null : config.nodeTypes().toArray(String[]::new))
                        .register()
        );
    }

    public void unregister() {
        registrations.forEach(registration ->
                Exceptions.wrap().run(registration::unregister)
        );
    }
}
