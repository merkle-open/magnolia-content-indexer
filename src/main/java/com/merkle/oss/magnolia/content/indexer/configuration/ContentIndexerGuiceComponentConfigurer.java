package com.merkle.oss.magnolia.content.indexer.configuration;

import info.magnolia.objectfactory.guice.AbstractGuiceComponentConfigurer;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.merkle.oss.magnolia.content.indexer.annotation.IndexerFactories;

public class ContentIndexerGuiceComponentConfigurer extends AbstractGuiceComponentConfigurer {
	@Override
	protected void configure() {
		super.configure();
		Multibinder.newSetBinder(binder(), new TypeLiteral<Class<?>>(){}, IndexerFactories.class);
	}
}
