package io.opentracing.contrib.examples.multiple_callbacks;

import static com.jayway.awaitility.Awaitility.await;
import static io.opentracing.contrib.examples.TestUtils.reportedSpansSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.opentracing.ContinuableScope;
import io.opentracing.Scope;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.usecases.AutoFinishScopeManager;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class TestMultipleCallbacks {

  private final MockTracer tracer = new MockTracer(Propagator.TEXT_MAP);

  @Test
  public void test() throws Exception {
    tracer.setScopeManager(new AutoFinishScopeManager());

    Client client = new Client(tracer);
    try (ContinuableScope scope = (ContinuableScope)tracer.buildSpan("parent").startActive()) {
      client.send("task1", scope, 300);
      client.send("task2", scope, 200);
      client.send("task3", scope, 100);
    }

    await().atMost(15, TimeUnit.SECONDS).until(reportedSpansSize(tracer), equalTo(4));

    List<MockSpan> spans = tracer.finishedSpans();
    assertEquals(4, spans.size());
    assertEquals("parent", spans.get(3).operationName());

    MockSpan parentSpan = spans.get(3);
    for (int i = 0; i < 3; i++) {
      assertEquals(true, parentSpan.finishMicros() >= spans.get(i).finishMicros());
      assertEquals(parentSpan.context().traceId(), spans.get(i).context().traceId());
      assertEquals(parentSpan.context().spanId(), spans.get(i).parentId());
    }

    assertNull(tracer.scopeManager().active());
  }
}
