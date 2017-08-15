package io.opentracing.contrib.examples.common_request_handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import io.opentracing.ActiveSpan;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

/**
 * There is only one instance of 'RequestHandler' per 'Client'. Methods of 'RequestHandler' are
 * executed concurrently in different threads which are reused (common pool). Therefore we cannot
 * use current active span and activate span. So one issue here is that we cannot create
 * parent-child relation.
 */
public class TestHandler {

  private final MockTracer tracer = new MockTracer(new ThreadLocalActiveSpanSource(),
      Propagator.TEXT_MAP);
  private final Client client = new Client(new RequestHandler(tracer));

  @Before
  public void before() {
    tracer.reset();
  }

  @Test
  public void test() throws Exception {
    Future<Object> responseFuture = client.send("message");
    Future<Object> responseFuture2 = client.send("message2");

    assertEquals("message:response", responseFuture.get(15, TimeUnit.SECONDS));
    assertEquals("message2:response", responseFuture2.get(15, TimeUnit.SECONDS));

    List<MockSpan> finished = tracer.finishedSpans();
    assertEquals(2, finished.size());

    for (MockSpan span : finished) {
      assertEquals(Tags.SPAN_KIND_CLIENT, span.tags().get(Tags.SPAN_KIND.getKey()));
    }

    assertNotEquals(finished.get(0).context().traceId(), finished.get(1).context().traceId());
    assertEquals(finished.get(0).parentId(), finished.get(1).parentId());

    assertNull(tracer.activeSpan());
  }

  /**
   * there is no way to create parent-child relation
   */
  @Test
  public void never_parent() throws Exception {
    try (ActiveSpan parent = tracer.buildSpan("parent").startActive()) {
      Object response = client.send("no_parent").get(15, TimeUnit.SECONDS);
      assertEquals("no_parent:response", response);
    }

    List<MockSpan> finished = tracer.finishedSpans();
    assertEquals(2, finished.size());

    MockSpan child = getByOperationName(finished, RequestHandler.OPERATION_NAME);
    assertNotNull(child);

    MockSpan parent = getByOperationName(finished, "parent");
    assertNotNull(parent);

    // Here check that there is no parent-child relation although it should be because child is
    // created when parent is active
    assertNotEquals(parent.context().spanId(), child.parentId());
  }

  private static MockSpan getByOperationName(List<MockSpan> spans, String name) {
    for (MockSpan span : spans) {
      if (name.equals(span.operationName())) {
        return span;
      }
    }
    return null;
  }
}
