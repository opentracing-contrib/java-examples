package io.opentracing.contrib.examples.request_context;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;
import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * If we create a wrapper for ActiveSpanSource then we won't able to obtain underlying Span inside
 * ActiveSpanSource.activeSpan. That's why wrapper is insufficient, we need our own implementation :(
 * <p>
 * Would be nice to reuse ThreadLocalActiveSpan but it sits in another package and has package protected constructor :(((
 * <p>
 * I've just copied here whole implementation of ThreadLocalActiveSpan and made it expose underlying Span.
 */
public class CustomActiveSpanSource implements ActiveSpanSource {
    final ThreadLocal<ThreadLocalActiveSpanCopy> tlsSnapshot = new ThreadLocal<ThreadLocalActiveSpanCopy>();

    @Override
    public ThreadLocalActiveSpanCopy activeSpan() {
        return tlsSnapshot.get();
    }

    @Override
    public ActiveSpan makeActive(Span span) {
        // This is too late, Tracer should create WrappedSpans, not active span source!
        WrappedSpan wrapped = new WrappedSpan(span, null);
        return new ThreadLocalActiveSpanCopy(this, wrapped, new AtomicInteger(1));
    }

    /**
     * Like normal ThreadLocalActiveSpan but has getWrapped() method.
     */
    static class ThreadLocalActiveSpanCopy implements ActiveSpan {
        private final CustomActiveSpanSource source;
        private final Span wrapped;
        private final ThreadLocalActiveSpanCopy toRestore;
        private final AtomicInteger refCount;

        ThreadLocalActiveSpanCopy(CustomActiveSpanSource source, Span wrapped, AtomicInteger refCount) {
            this.source = source;
            this.refCount = refCount;
            this.wrapped = wrapped;
            this.toRestore = source.tlsSnapshot.get();
            source.tlsSnapshot.set(this);
        }

        public Span getWrapped() {
            return wrapped;
        }

        @Override
        public void deactivate() {
            if (source.tlsSnapshot.get() != this) {
                // This shouldn't happen if users call methods in the expected order. Bail out.
                return;
            }
            source.tlsSnapshot.set(toRestore);

            if (0 == refCount.decrementAndGet()) {
                wrapped.finish();
            }
        }

        @Override
        public ThreadLocalActiveSpanCopy.Continuation capture() {
            return new ThreadLocalActiveSpanCopy.Continuation();
        }

        @Override
        public SpanContext context() {
            return wrapped.context();
        }

        @Override
        public ThreadLocalActiveSpanCopy setTag(String key, String value) {
            wrapped.setTag(key, value);
            return this;
        }

        @Override
        public ThreadLocalActiveSpanCopy setTag(String key, boolean value) {
            wrapped.setTag(key, value);
            return this;
        }

        @Override
        public ThreadLocalActiveSpanCopy setTag(String key, Number value) {
            wrapped.setTag(key, value);
            return this;
        }

        @Override
        public ThreadLocalActiveSpanCopy log(Map<String, ?> fields) {
            wrapped.log(fields);
            return this;
        }

        @Override
        public ThreadLocalActiveSpanCopy log(long timestampMicroseconds, Map<String, ?> fields) {
            wrapped.log(timestampMicroseconds, fields);
            return this;
        }

        @Override
        public ThreadLocalActiveSpanCopy log(String event) {
            wrapped.log(event);
            return this;
        }

        @Override
        public ThreadLocalActiveSpanCopy log(long timestampMicroseconds, String event) {
            wrapped.log(timestampMicroseconds, event);
            return this;
        }

        @Override
        public ThreadLocalActiveSpanCopy setBaggageItem(String key, String value) {
            wrapped.setBaggageItem(key, value);
            return this;
        }

        @Override
        public String getBaggageItem(String key) {
            return wrapped.getBaggageItem(key);
        }

        @Override
        public ThreadLocalActiveSpanCopy setOperationName(String operationName) {
            wrapped.setOperationName(operationName);
            return this;
        }

        @Override
        public ThreadLocalActiveSpanCopy log(String eventName, Object payload) {
            wrapped.log(eventName, payload);
            return this;
        }

        @Override
        public ThreadLocalActiveSpanCopy log(long timestampMicroseconds, String eventName, Object payload) {
            wrapped.log(timestampMicroseconds, eventName, payload);
            return this;
        }

        @Override
        public void close() {
            deactivate();
        }

        private final class Continuation implements ActiveSpan.Continuation {
            Continuation() {
                refCount.incrementAndGet();
            }

            @Override
            public ThreadLocalActiveSpanCopy activate() {
                return new ThreadLocalActiveSpanCopy(source, wrapped, refCount);
            }
        }

    }
}
