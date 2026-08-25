package com.merkle.oss.magnolia.content.indexer.registry;

import info.magnolia.config.NamedDefinition;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import javax.jcr.observation.Event;

import com.merkle.oss.magnolia.content.indexer.Config;
import com.merkle.oss.magnolia.content.indexer.Indexer;

public class IndexerDefinition implements NamedDefinition {
	private final String name;
	private final Class<? extends Indexer> clazz;
    private final int batchSize;
    private final Predicate<Event> filter;
    private final Set<Config> configs;

	public IndexerDefinition(
			final String name,
			final Class<? extends Indexer> clazz,
			final int batchSize,
			final Predicate<Event> filter,
			final Set<Config> configs
	) {
		this.name = name;
		this.clazz = clazz;
        this.batchSize = batchSize;
        this.filter = filter;
        this.configs = configs;
    }

	@Override
	public String getName() {
		return name;
	}

	public Class<? extends Indexer> getClazz() {
		return clazz;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public Set<Config> getConfigs() {
		return configs;
	}

	public Predicate<Event> getFilter() {
		return filter;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IndexerDefinition that)) {
			return false;
		}
        return batchSize == that.batchSize && Objects.equals(name, that.name) && Objects.equals(clazz, that.clazz) && Objects.equals(filter, that.filter) && Objects.equals(configs, that.configs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, clazz, batchSize, filter, configs);
	}

	@Override
	public String toString() {
		return "IndexerDefinition{" +
				"name='" + name + '\'' +
				", clazz=" + clazz +
				", batchSize=" + batchSize +
				", filter=" + filter +
				", configs=" + configs +
				'}';
	}
}
