package io.opentracing.contrib.examples.common_request_handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import java.util.List;
import java.util.concurrent.Future;
import org.junit.Test;

/**
 * There is only one instance of 'RequestHandler' per 'Client'. Methods of 'RequestHandler' are
 * executed concurrently in different threads which are reused (common pool). Therefore we cannot
 * use current active span and activate span.
 */
public class TestHandler {

  private final MockTracer tracer = new MockTracer(new ThreadLocalActiveSpanSource(),
      Propagator.TEXT_MAP);

  @Test
  public void test() throws Exception {
    Client client = new Client(new RequestHandler(tracer));

    Future<Object> responseFuture = client.send("message");
    Future<Object> responseFuture2 = client.send("message2");

    assertEquals("message:response", responseFuture.get());
    assertEquals("message2:response", responseFuture2.get());

    List<MockSpan> finished = tracer.finishedSpans();
    assertEquals(2, finished.size());

    for (MockSpan span : finished) {
      assertEquals(Tags.SPAN_KIND_CLIENT, span.tags().get(Tags.SPAN_KIND.getKey()));
    }

    assertNotEquals(finished.get(0).context().traceId(), finished.get(1).context().traceId());
    assertEquals(finished.get(0).parentId(), finished.get(1).parentId());

    assertNull(tracer.activeSpan());
  }
}
