package io.opentracing.contrib.examples.request_context;

import io.opentracing.*;

import java.util.concurrent.ConcurrentHashMap;

import static io.opentracing.contrib.examples.request_context.CustomActiveSpanSource.ThreadLocalActiveSpanCopy;

/**
 * SpanBuilder which creates Spans with RequestContext. Propagates RequestContext from parent span to child.
 * <p>
 * Note1 - it works only when calling asChildOf(BaseSpan). asChildOf(SpanContext) has no way to obtain
 * parent's RequestContext.
 * <p>
 * Note2 - startActive starts a span but returns active span. We assume that the wrapped SpanBuilder will call
 * startManual first (so that we can wrap the span) and then makeActive()
 * <p>
 * Note3 - unfortunately we can't wrap startManual properly as the wrapped tracer calls it's own (not wrapped) startManual.
 * That's why startActive has this weird code.
 */
public class SpanBuilderWrapper implements Tracer.SpanBuilder {
    private final Tracer.SpanBuilder wrapped;
    private ConcurrentHashMap<String, Object> withRequestContext;

    public SpanBuilderWrapper(Tracer.SpanBuilder wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ActiveSpan startActive() {
        // this is a hack because wrapped Tracer calls it's own startManual, not startManual from this class
        // so we add parent request context values explicitly here
        ThreadLocalActiveSpanCopy activeSpan = (ThreadLocalActiveSpanCopy) wrapped.startActive();
        WrappedSpan wrapped = (WrappedSpan) activeSpan.getWrapped();
        if(withRequestContext != null) {
            wrapped.getRequestContext().putAll(withRequestContext);
        }
        return activeSpan;
    }

    @Override
    public Span startManual() {
        return new WrappedSpan(wrapped.startManual(), withRequestContext);
    }

    @Override
    @Deprecated
    public Span start() {
        return new WrappedSpan(wrapped.start(), withRequestContext);
    }

    @Override
    public Tracer.SpanBuilder asChildOf(SpanContext parent) {
        wrapped.asChildOf(parent);
        return this;
    }

    @Override
    public Tracer.SpanBuilder asChildOf(BaseSpan<?> parent) {
        WrappedSpan parentWrapped;
        if (parent instanceof ThreadLocalActiveSpanCopy) {
            ThreadLocalActiveSpanCopy parentActiveSpan = (ThreadLocalActiveSpanCopy) parent;
            parentWrapped = (WrappedSpan) parentActiveSpan.getWrapped();
        } else {
            parentWrapped = (WrappedSpan) parent;
        }
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
