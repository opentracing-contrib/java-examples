package io.opentracing.contrib.examples.activate_deactivate;

import static com.jayway.awaitility.Awaitility.await;
import static io.opentracing.contrib.examples.TestUtils.reportedSpansSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;

import io.opentracing.ActiveSpan;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCallback {

  private static final Logger logger = LoggerFactory.getLogger(TestCallback.class);

  private final MockTracer tracer = new MockTracer(new ThreadLocalActiveSpanSource(),
      Propagator.TEXT_MAP);
  private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

  @Test
  public void test() throws Exception {
    Thread entryThread = entryThread();
    entryThread.start();
    entryThread.join(10_000);
    // Entry thread is completed but Callback is still running (or even not started)

    await().atMost(15, TimeUnit.SECONDS).until(reportedSpansSize(tracer), equalTo(1));

    List<MockSpan> finished = tracer.finishedSpans();
    assertEquals(1, finished.size());

    assertEquals(200, finished.get(0).tags().get(Tags.HTTP_STATUS.getKey()));
  }

  /**
   * Thread will be completed before callback completed.
   */
  private Thread entryThread() {
    return new Thread(new Runnable() {
      @Override
      public void run() {
        logger.info("Entry thread started");
        ActiveSpan activeSpan = tracer.buildSpan("parent").startActive();
        Runnable callback = new Callback(activeSpan);

        // Callback is executed at some unpredictable time and we are not able to check status of the callback
        service.schedule(callback, 500, TimeUnit.MILLISECONDS);

        activeSpan.deactivate();
        logger.info("Entry thread finished");
      }
    });
  }
}
