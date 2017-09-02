package io.opentracing.contrib.examples.request_context;

import io.opentracing.ScopeManager;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

public class TracerWrapper implements Tracer {
    private final Tracer wrapped;

    public TracerWrapper(Tracer wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ScopeManager scopeManager() {
        return wrapped.scopeManager();
    }

    @Override
    public void setScopeManager(ScopeManager scopeManager) {
        wrapped.setScopeManager(scopeManager);
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new SpanBuilderWrapper(this, wrapped.buildSpan(operationName));
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        wrapped.inject(spanContext, format, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return wrapped.extract(format, carrier);
    }
}
