package io.opentracing.contrib.examples.listener_per_request;

import static io.opentracing.contrib.examples.TestUtils.getOneByTag;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import java.util.List;
import org.junit.Test;

/**
 * Each request has own instance of ResponseListener
 */
public class TestListener {

  private final MockTracer tracer = new MockTracer(new ThreadLocalActiveSpanSource(),
      Propagator.TEXT_MAP);

  @Test
  public void test() throws Exception {
    Client client = new Client(tracer);
    Object response = client.send("message").get();
    assertEquals("message:response", response);

    List<MockSpan> finished = tracer.finishedSpans();
    assertEquals(1, finished.size());
    assertNotNull(getOneByTag(finished, Tags.SPAN_KIND, Tags.SPAN_KIND_CLIENT));
    assertNull(tracer.activeSpan());
  }
}
