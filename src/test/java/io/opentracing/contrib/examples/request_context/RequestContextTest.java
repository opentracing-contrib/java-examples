package io.opentracing.contrib.examples.request_context;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.ThreadLocalScopeManager;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RequestContextTest {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private Tracer createTracer() {
        MockTracer mockTracer = new MockTracer(MockTracer.Propagator.TEXT_MAP);
        mockTracer.setScopeManager(new ThreadLocalScopeManager());
        return new TracerWrapper(mockTracer);
    }
    @Test
    public void singleSpan() {
        Tracer tracer = createTracer();
        RequestContext ctx = new RequestContext(tracer);

        try (Scope scope = tracer.buildSpan("x").startActive()) {
            ctx.put("current_user", new User(1, "john"));

            User user = ctx.get("current_user", User.class);

            assertEquals("john", user.name);
        }
    }

    @Test
    public void propagatesToChildSpans() {
        Tracer tracer = createTracer();
        RequestContext ctx = new RequestContext(tracer);

        try (Scope parent = tracer.buildSpan("parent").startActive()) {
            ctx.put("current_user", new User(1, "john"));

            try (Scope child = tracer.buildSpan("child")
                    .asChildOf(parent.span()) // explicit because MockTracer does not add this automatically
                    .startActive()) {
                User user = ctx.get("current_user", User.class);

                assertEquals("john", user.name);
            }
        }
    }

    @Test
    public void changesInChildSpansDoNotAlterParent() {
        Tracer tracer = createTracer();
        RequestContext ctx = new RequestContext(tracer);

        try (Scope parent = tracer.buildSpan("parent").startActive()) {
            ctx.put("current_user", new User(1, "john"));

            try (Scope child = tracer.buildSpan("child").startActive()) {
                ctx.put("current_user", new User(2, "bob"));
            }

            assertEquals(ctx.get("current_user", User.class).name, "john");
        }
    }

    @Test
    public void keysSetInChildNotVisibleInParent() {
        Tracer tracer = createTracer();
        RequestContext ctx = new RequestContext(tracer);

        try (Scope parent = tracer.buildSpan("parent").startActive()) {
            try (Scope child = tracer.buildSpan("child").startActive()) {
                ctx.put("current_user", new User(2, "bob"));
            }
            assertNull(ctx.get("current_user", User.class));
        }
    }

    @Test
    public void propagatesToOtherThreads() throws Exception {
        final Tracer tracer = createTracer();
        final RequestContext ctx = new RequestContext(tracer);
        final AtomicReference<User> threadUser = new AtomicReference<>();
        try (Scope scope = tracer.buildSpan("parent").startActive()) {
            ctx.put("current_user", new User(1, "john"));

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    // instead of Continuations we pass and activate a Span
                    tracer.scopeManager().activate(scope.span());
                    User innerUser = ctx.get("current_user", User.class);
                    threadUser.set(innerUser);
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
