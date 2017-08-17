package io.opentracing.contrib.examples.nested_callbacks;

import static com.jayway.awaitility.Awaitility.await;
import static io.opentracing.contrib.examples.TestUtils.reportedSpansSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;

import io.opentracing.ActiveSpan;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class TestNestedCallbacks {

  private final MockTracer tracer = new MockTracer(new ThreadLocalActiveSpanSource(),
      Propagator.TEXT_MAP);
  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Test
  public void test() throws Exception {

    try (ActiveSpan span = tracer.buildSpan("one").startActive()) {
      submitCallbacks(span);
    }

    await().atMost(15, TimeUnit.SECONDS).until(reportedSpansSize(tracer), equalTo(1));

    List<MockSpan> spans = tracer.finishedSpans();
    assertEquals(1, spans.size());
    assertEquals("one", spans.get(0).operationName());

    Map<String, Object> tags = spans.get(0).tags();
    assertEquals(3, tags.size());
    for (int i = 1; i <= 3; i++)
      assertEquals(Integer.toString(i), tags.get("key" + i));
  }

  void submitCallbacks(ActiveSpan span) {
    final ActiveSpan.Continuation cont = span.capture();

    executor.submit(new Runnable() {
      @Override
      public void run() {
        try (ActiveSpan span = cont.activate()) {
          span.setTag("key1", "1");
          final ActiveSpan.Continuation cont = span.capture();

          executor.submit(new Runnable() {
            @Override
            public void run() {
              try (ActiveSpan span = cont.activate()) {
                span.setTag("key2", "2");
                final ActiveSpan.Continuation cont = span.capture();

                executor.submit(new Runnable() {
                  @Override
                  public void run() {
                    try (ActiveSpan span = cont.activate()) {
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
