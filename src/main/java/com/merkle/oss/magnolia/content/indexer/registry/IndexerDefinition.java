package com.merkle.oss.magnolia.content.indexer.registry;

import info.magnolia.config.NamedDefinition;

import java.util.Objects;
import java.util.Set;

import com.merkle.oss.magnolia.content.indexer.Config;
import com.merkle.oss.magnolia.content.indexer.Indexer;

public class IndexerDefinition implements NamedDefinition {
	private final String name;
	private final Class<? extends Indexer> clazz;
    private final Set<Config> configs;

	public IndexerDefinition(
			final String name,
			final Class<? extends Indexer> clazz,
			final Set<Config> configs
	) {
		this.name = name;
		this.clazz = clazz;
        this.configs = configs;
    }

	@Override
	public String getName() {
		return name;
	}

	public Class<? extends Indexer> getClazz() {
		return clazz;
	}

	public Set<Config> getConfigs() {
		return configs;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IndexerDefinition that = (IndexerDefinition) o;
		return Objects.equals(name, that.name) && Objects.equals(clazz, that.clazz) && Objects.equals(configs, that.configs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, clazz, configs);
	}

	@Override
	public String toString() {
		return "IndexerDefinition{" +
				"name='" + name + '\'' +
				", clazz=" + clazz +
				", configs=" + configs +
				'}';
	}
}
