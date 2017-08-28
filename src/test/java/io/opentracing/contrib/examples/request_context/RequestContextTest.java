package io.opentracing.contrib.examples.request_context;

import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static io.opentracing.ActiveSpan.Continuation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RequestContextTest {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Test
    public void singleSpan() {
        Tracer tracer = new TracerWrapper(new MockTracer(new CustomActiveSpanSource(), MockTracer.Propagator.TEXT_MAP));
        RequestContext ctx = new RequestContext(tracer);

        try (ActiveSpan span = tracer.buildSpan("x").startActive()) {
            ctx.put("current_user", new User(1, "john"));

            User user = ctx.get("current_user", User.class);

            assertEquals("john", user.name);
        }
    }

    @Test
    public void propagatesToChildSpans() {
        Tracer tracer = new TracerWrapper(new MockTracer(new CustomActiveSpanSource(), MockTracer.Propagator.TEXT_MAP));
        RequestContext ctx = new RequestContext(tracer);

        try (ActiveSpan parent = tracer.buildSpan("parent").startActive()) {
            ctx.put("current_user", new User(1, "john"));

            try (ActiveSpan child = tracer.buildSpan("child")
                    .asChildOf(parent) // explicit because MockTracer does not add this automatically
                    .startActive()) {
                User user = ctx.get("current_user", User.class);

                assertEquals("john", user.name);
            }
        }
    }

    @Test
    public void changesInChildSpansDoNotAlterParent() {
        Tracer tracer = new TracerWrapper(new MockTracer(new CustomActiveSpanSource(), MockTracer.Propagator.TEXT_MAP));
        RequestContext ctx = new RequestContext(tracer);

        try (ActiveSpan parent = tracer.buildSpan("parent").startActive()) {
            ctx.put("current_user", new User(1, "john"));

            try (ActiveSpan child = tracer.buildSpan("child").startActive()) {
                ctx.put("current_user", new User(2, "bob"));
            }

            assertEquals(ctx.get("current_user", User.class).name, "john");
        }
    }

    @Test
    public void keysSetInChildNotVisibleInParent() {
        Tracer tracer = new TracerWrapper(new MockTracer(new CustomActiveSpanSource(), MockTracer.Propagator.TEXT_MAP));
        RequestContext ctx = new RequestContext(tracer);

        try (ActiveSpan parent = tracer.buildSpan("parent").startActive()) {
            try (ActiveSpan child = tracer.buildSpan("child").startActive()) {
                ctx.put("current_user", new User(2, "bob"));
            }
            assertNull(ctx.get("current_user", User.class));
        }
    }

    @Test
    public void propagatesToOtherThreads() throws Exception {
        Tracer tracer = new TracerWrapper(new MockTracer(new CustomActiveSpanSource(), MockTracer.Propagator.TEXT_MAP));
        final RequestContext ctx = new RequestContext(tracer);
        final AtomicReference<User> threadUser = new AtomicReference<>();
        try (ActiveSpan span = tracer.buildSpan("parent").startActive()) {
            ctx.put("current_user", new User(1, "john"));
            final Continuation continuation = span.capture();
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try (ActiveSpan span2 = continuation.activate()) {
                        threadUser.set(ctx.get("current_user", User.class));
                    }
                }
            }).get();

            assertEquals(threadUser.get().name, "john");
        }
    }

    static class User {
        int id;
        String name;

        User(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
