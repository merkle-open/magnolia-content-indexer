package com.merkle.oss.magnolia.content.indexer.registry;

import info.magnolia.config.registry.AbstractRegistry;
import info.magnolia.config.registry.DefinitionType;
import info.magnolia.module.ModuleRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.merkle.oss.magnolia.content.indexer.Indexer;

@Singleton
public class IndexerDefinitionRegistry extends AbstractRegistry<IndexerDefinition> {
	public static Type TYPE = new Type();

	@Inject
	public IndexerDefinitionRegistry(final ModuleRegistry moduleRegistry) {
		super(moduleRegistry);
	}

	@Override
	public DefinitionType type() {
		return TYPE;
	}

	private static class Type implements DefinitionType {
		@Override
		public String getName() {
			return "indexer";
		}
		@Override
		public String name() {
			return getName();
		}
		@Override
		public Class<?> baseClass() {
			return Indexer.class;
		}
	}
}
