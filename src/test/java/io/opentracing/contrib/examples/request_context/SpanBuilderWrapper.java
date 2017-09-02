package io.opentracing.contrib.examples.request_context;

import io.opentracing.*;

import java.util.concurrent.ConcurrentHashMap;

/**
 * SpanBuilder which creates Spans with RequestContext. Propagates RequestContext from parent span to child.
 * <p>
 * Note - it works only when calling asChildOf(BaseSpan). asChildOf(SpanContext) has no way to obtain
 * parent's RequestContext.
 */
public class SpanBuilderWrapper implements Tracer.SpanBuilder {
    private final Tracer tracer;
    private final Tracer.SpanBuilder wrapped;
    private ConcurrentHashMap<String, Object> withRequestContext;

    public SpanBuilderWrapper(Tracer tracer, Tracer.SpanBuilder wrapped) {
        this.tracer = tracer;
        this.wrapped = wrapped;
    }

    @Override
    public Scope startActive() {
        // We don't delegate startActive so if underlying tracer does something additional - it will be lost
        return startManual().activate();
    }

    @Override
    public Scope startActive(Scope.Observer observer) {
        return this.wrapped.startManual().activate(observer);
    }

    @Override
    public Span startManual() {
        return new WrappedSpan(tracer, wrapped.startManual(), withRequestContext);
    }

    @Override
    @Deprecated
    public Span start() {
        return new WrappedSpan(tracer, wrapped.start(), withRequestContext);
    }

    @Override
    public Tracer.SpanBuilder asChildOf(SpanContext parent) {
        wrapped.asChildOf(parent);
        return this;
    }

    @Override
    public Tracer.SpanBuilder asChildOf(Span parent) {
        WrappedSpan parentWrapped = (WrappedSpan) parent;
        // copy request context from parent
        withRequestContext = new ConcurrentHashMap<>(parentWrapped.getRequestContext());
        wrapped.asChildOf(parent);
        return this;
    }

    @Override
    public Tracer.SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
        wrapped.addReference(referenceType, referencedContext);
        return this;
    }

    @Override
    public Tracer.SpanBuilder ignoreActiveSpan() {
        wrapped.ignoreActiveSpan();
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, String value) {
        wrapped.withTag(key, value);
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, boolean value) {
        wrapped.withTag(key, value);
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, Number value) {
        wrapped.withTag(key, value);
        return this;
    }

    @Override
    public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
        wrapped.withStartTimestamp(microseconds);
        return this;
    }
}
