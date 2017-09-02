package io.opentracing.contrib.examples.request_context;


import io.opentracing.Scope;
import io.opentracing.Tracer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A baggage-like context, but can hold any type of value and it not propagated outside of the process.
 * <p>
 * There are few ways to implement this. The main issue is how to kep track of the relation between context map and
 * a Span when Spans have no identity guaranteed by the API. I see following options:
 * 1. Wrap ActiveSpanSource, maybe also ActiveSpan/Span and provide identity
 * 2. Wrap Span and store the context inside it
 * <p>
 * If we go with 1. then we as well can do 2., so this implements the 2nd approach.
 * <p>
 * Both approaches (adding context map or identity to span) have a drawback though - we will need to downcast to our
 * WrappedSpan. If the span is wrapped with another wrapper the casting will fail.
 * <p>
 * But! That's not all. We need to obtain our current (active) WrappedSpan. But ActiveSpan does not provide such method.
 * So we also need to wrap ActiveSpan to add a method returning the span :(
 */
public class RequestContext {
    private final Tracer tracer;

    public RequestContext(Tracer tracer) {
        this.tracer = tracer;
    }

    public <T> T get(String key, Class<T> valueClass) {
        Map<String, Object> context = getContext();
        if (context == null) {
            return null;
        }

        //noinspection unchecked
        return (T) context.get(key);
    }

    public void put(String key, Object value) {
        Map<String, Object> context = getContext();
        if (context != null) {
            context.put(key, value);
        }
    }

    private Map<String, Object> getContext() {
        Scope scope = tracer.scopeManager().active();
        if (scope != null) {
            // !! this works with only our span wrapper, other wrappers will break it
            WrappedSpan wrapped = (WrappedSpan) scope.span();
            return wrapped.getRequestContext();
        }
        return null;
    }
}
