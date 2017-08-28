package io.opentracing.contrib.examples.request_context;

import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WrappedSpan implements Span {
    private final Span wrapped;
    private Map<String, Object> requestContext;

    public WrappedSpan(Span wrapped, ConcurrentHashMap<String, Object> requestContext) {
        this.wrapped = wrapped;
        if (requestContext != null) {
            this.requestContext = requestContext;
        } else {
            this.requestContext = new ConcurrentHashMap<>();
        }
    }

    public Map<String, Object> getRequestContext() {
        return requestContext;
    }

    @Override
    public void finish() {
        wrapped.finish();
    }

    @Override
    public void finish(long finishMicros) {
        wrapped.finish(finishMicros);
    }

    @Override
    public SpanContext context() {
        return wrapped.context();
    }

    @Override
    public Span setTag(String key, String value) {
        return wrapped.setTag(key, value);
    }

    @Override
    public Span setTag(String key, boolean value) {
        return wrapped.setTag(key, value);
    }

    @Override
    public Span setTag(String key, Number value) {
        return wrapped.setTag(key, value);
    }

    @Override
    public Span log(Map<String, ?> fields) {
        return wrapped.log(fields);
    }

    @Override
    public Span log(long timestampMicroseconds, Map<String, ?> fields) {
        return wrapped.log(timestampMicroseconds, fields);
    }

    @Override
    public Span log(String event) {
        return wrapped.log(event);
    }

    @Override
    public Span log(long timestampMicroseconds, String event) {
        return wrapped.log(timestampMicroseconds, event);
    }

    @Override
    public Span setBaggageItem(String key, String value) {
        return wrapped.setBaggageItem(key, value);
    }

    @Override
    public String getBaggageItem(String key) {
        return wrapped.getBaggageItem(key);
    }

    @Override
    public Span setOperationName(String operationName) {
        return wrapped.setOperationName(operationName);
    }

    @Override
    @Deprecated
    public Span log(String eventName, Object payload) {
        return wrapped.log(eventName, payload);
    }

    @Override
    @Deprecated
    public Span log(long timestampMicroseconds, String eventName, Object payload) {
        return wrapped.log(timestampMicroseconds, eventName, payload);
    }
}
