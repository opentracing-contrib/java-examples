package io.opentracing.contrib.examples.nested_callbacks;

import static com.jayway.awaitility.Awaitility.await;
import static io.opentracing.contrib.examples.TestUtils.reportedSpansSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.opentracing.Scope;
import io.opentracing.Scope.Observer;
import io.opentracing.Span;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.util.ThreadLocalScopeManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class TestNestedCallbacks {

  private final MockTracer tracer = new MockTracer(Propagator.TEXT_MAP);
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Before
  public void before() {
    tracer.reset();
    tracer.setScopeManager(new ThreadLocalScopeManager());
  }

  @Test
  public void test() throws Exception {
    /* Start a Span and let the callback-chain
     * finish it when the task is done */
    submitCallbacks(tracer.buildSpan("one").startManual());

    await().atMost(15, TimeUnit.SECONDS).until(reportedSpansSize(tracer), equalTo(1));

    List<MockSpan> spans = tracer.finishedSpans();
    assertEquals(1, spans.size());
    assertEquals("one", spans.get(0).operationName());

    Map<String, Object> tags = spans.get(0).tags();
    assertEquals(3, tags.size());
    for (int i = 1; i <= 3; i++) {
      assertEquals(Integer.toString(i), tags.get("key" + i));
    }

    assertNull(tracer.scopeManager().active());
  }

  private void submitCallbacks(final Span span) {

    executor.submit(new Runnable() {
      @Override
      public void run() {
        try (Scope scope = span.activate()) {
          span.setTag("key1", "1");

          executor.submit(new Runnable() {
            @Override
            public void run() {
              try (Scope scope = span.activate()) {
                span.setTag("key2", "2");

                executor.submit(new Runnable() {
                  @Override
                  public void run() {
                    /* Decide explicitly when to finish the Span */
                    try (Scope scope = span.activate(Observer.FINISH_ON_CLOSE)) {
                      span.setTag("key3", "3");
                    }
                  }
                });
              }
            }
          });
        }
      }
    });
  }
}
