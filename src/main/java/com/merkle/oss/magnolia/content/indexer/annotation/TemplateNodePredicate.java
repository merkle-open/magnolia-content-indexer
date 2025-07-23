package com.merkle.oss.magnolia.content.indexer.annotation;

import info.magnolia.jcr.util.NodeTypes;

import java.util.Objects;

import javax.jcr.Node;

import com.machinezoo.noexception.Exceptions;

public abstract class TemplateNodePredicate implements NodePredicate {
    private final String template;

    protected TemplateNodePredicate(final String template) {
        this.template = template;
    }

    @Override
    public boolean test(final Node node) {
        return Objects.equals(template, Exceptions.wrap().get(() -> NodeTypes.Renderable.getTemplate(node)));
    }
}
