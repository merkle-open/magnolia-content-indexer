# Magnolia ContentIndexer

The content indexer module makes can be used to populate search indexes.

## Requirements
* Java 17
* Magnolia >= 6.3

## Setup

### Add Maven dependency:
```xml
<dependency>
    <groupId>com.merkle.oss.magnolia</groupId>
    <artifactId>magnolia-content-indexer</artifactId>
    <version>0.0.6</version>
</dependency>
```

### DI-Bindings
```xml
<module>
    <name>SomeModule</name>
    ...
    <components>
        <id>main</id>
        <configurer>
            <class>GuiceComponentConfigurer</class>
        </configurer>
    </components>
    ...
</module>
```

```java
import info.magnolia.objectfactory.guice.AbstractGuiceComponentConfigurer;
import info.magnolia.virtualuri.VirtualUriMapping;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.reflections.Reflections;

import com.google.inject.multibindings.Multibinder;
import com.merkle.oss.magnolia.content.indexer.annotation.IndexerFactories;
import com.merkle.oss.magnolia.content.indexer.annotation.IndexerFactory;

public class GuiceComponentConfigurer extends AbstractGuiceComponentConfigurer {
    @Override
    protected void configure() {
        // Here we use Reflections, but you can also use ClassPathScanningCandidateComponentProvider or bind each factory manually 
        final Multibinder<Class<?>> indexerFactoryMultibinder = Multibinder.newSetBinder(binder, new TypeLiteral<>() {}, IndexerFactories.class);
        new Reflections(getClass()).getTypesAnnotatedWith(IndexerFactory.class).forEach(clazz -> indexerFactoryMultibinder.addBinding().toInstance(clazz));
    }
}
```

## How to use

## Example

```java
import com.merkle.oss.magnolia.content.indexer.Indexer;
import com.merkle.oss.magnolia.content.indexer.annotation.IndexerFactory;

import info.magnolia.dam.jcr.DamConstants;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;

import java.util.Collection;

import javax.jcr.Node;

@IndexerFactory(
        id = "SomeApp:indexers/" + SomeIndexer.NAME,
        name = SomeIndexer.NAME,
        configs = {
                @IndexerFactory.Config(type = "page", workspace = RepositoryConstants.WEBSITE, nodeTypes = { NodeTypes.Page.NAME }),
                @IndexerFactory.Config(type = "asset", workspace = DamConstants.WORKSPACE, nodeTypes = { "mgnl:asset" })
        }
)
public class SomeIndexer implements Indexer {
    public static final String NAME = "some";

    @Override
    public void index(final Collection<Node> nodes, final String type) throws Exception {
        /*
         * TODO add to index
         *  Gets triggered on node changes according to configs
         *  make sure to add node identifier to index to be able to remove it (see remove interface)
         * 
         *  NOTE: runs in system context not in web context!
         */
    }

    @Override
    public void remove(final Collection<Indexer.IndexNode> nodes, final String type) throws Exception {
        /*
         * TODO remove from index
         *  Gets triggered on node changes according to configs
         *
         *  NOTE: runs in system context not in web context!
         */
    }
}
```
