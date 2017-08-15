package io.opentracing.contrib.examples.client_server;

import static com.jayway.awaitility.Awaitility.await;
import static io.opentracing.contrib.examples.TestUtils.getByTag;
import static io.opentracing.contrib.examples.TestUtils.reportedSpansSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestClientServer {

  private final MockTracer tracer = new MockTracer(new ThreadLocalActiveSpanSource(),
      Propagator.TEXT_MAP);
  private final ArrayBlockingQueue<Message> queue = new ArrayBlockingQueue<>(10);
  private Server server;

  @Before
  public void before() {
    server = new Server(queue, tracer);
    server.start();
  }

  @After
  public void after() throws InterruptedException {
    server.interrupt();
    server.join(5_000L);
  }

  @Test
  public void test() throws Exception {
    Client client = new Client(queue, tracer);
    client.send();

    await().atMost(15, TimeUnit.SECONDS).until(reportedSpansSize(tracer), equalTo(2));

    List<MockSpan> finished = tracer.finishedSpans();
    assertEquals(2, finished.size());
    assertEquals(finished.get(0).context().traceId(), finished.get(1).context().traceId());
    assertNotNull(getByTag(finished, Tags.SPAN_KIND, Tags.SPAN_KIND_CLIENT));
    assertNotNull(getByTag(finished, Tags.SPAN_KIND, Tags.SPAN_KIND_SERVER));
    assertNull(tracer.activeSpan());
  }
}
