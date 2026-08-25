package com.merkle.oss.magnolia.content.indexer;


import info.magnolia.cms.util.FilteredEventListener;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.observation.WorkspaceEventListenerRegistration;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;

import com.machinezoo.noexception.Exceptions;
import com.merkle.oss.magnolia.content.indexer.registry.IndexerDefinition;
import com.merkle.oss.magnolia.content.indexer.registry.IndexerDefinitionRegistry;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class DataListenerRegistrar {
    private final IndexerDefinitionRegistry indexerDefinitionRegistry;
    private final SystemContext systemContext;
    private final ComponentProvider componentProvider;
    private final EventFilter eventFilter;
    private final Provider<ContentIndexerModule> contentIndexerModule;
    private final Set<WorkspaceEventListenerRegistration.Handle> registrations = new HashSet<>();

    @Inject
    public DataListenerRegistrar(
            final IndexerDefinitionRegistry indexerDefinitionRegistry,
            final SystemContext systemContext,
            final ComponentProvider componentProvider,
            final EventFilter eventFilter,
            final Provider<ContentIndexerModule> contentIndexerModule
    ) {
        this.indexerDefinitionRegistry = indexerDefinitionRegistry;
        this.systemContext = systemContext;
        this.componentProvider = componentProvider;
        this.eventFilter = eventFilter;
        this.contentIndexerModule = contentIndexerModule;
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
        final DataListener eventListener = new DataListener(systemContext, indexer, definition, config, eventFilter, contentIndexerModule);
        registrations.add(
                WorkspaceEventListenerRegistration
                        .observe(config.workspace(), config.rootNodePath(), new FilteredEventListener(eventListener, new AbstractPredicate<Event>() {
                            @Override
                            public boolean evaluateTyped(final Event event) {
                                return Optional.ofNullable(config.filter()).orElseGet(definition::getFilter).test(event);
                            }
                        }))
                        .withDelay(config.delay().toMillis())
                        .register()
        );
    }

    public void unregister() {
        registrations.forEach(registration ->
                Exceptions.wrap().run(registration::unregister)
        );
    }
}
