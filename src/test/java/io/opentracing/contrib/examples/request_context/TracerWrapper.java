package io.opentracing.contrib.examples.request_context;

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

public class TracerWrapper implements Tracer {
    private final Tracer wrapped;

    public TracerWrapper(Tracer wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new SpanBuilderWrapper(wrapped.buildSpan(operationName));
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        wrapped.inject(spanContext, format, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return wrapped.extract(format, carrier);
    }

    @Override
    public ActiveSpan activeSpan() {
        return wrapped.activeSpan();
    }

    @Override
    public ActiveSpan makeActive(Span span) {
        return wrapped.makeActive(span);
    }
}
