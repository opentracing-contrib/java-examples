package io.opentracing.contrib.examples.activate_deactivate;

import static com.jayway.awaitility.Awaitility.await;
import static io.opentracing.contrib.examples.TestUtils.reportedSpansSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;

import io.opentracing.ContinuableScope;
import io.opentracing.Scope;
import io.opentracing.Scope.Observer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.usecases.AutoFinishScopeManager;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCallback {

  private static final Logger logger = LoggerFactory.getLogger(TestCallback.class);

  private final MockTracer tracer = new MockTracer(Propagator.TEXT_MAP);
  private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

  @Before
  public void before() {
    tracer.reset();
    tracer.setScopeManager(new AutoFinishScopeManager());
  }

  @Test
  public void test() throws Exception {
    Thread entryThread = entryThread();
    entryThread.start();
    entryThread.join(10_000);
    // Entry thread is completed but Callback is still running (or even not started)

    await().atMost(15, TimeUnit.SECONDS).until(reportedSpansSize(tracer), equalTo(1));

    List<MockSpan> finished = tracer.finishedSpans();
    assertEquals(1, finished.size());

    assertEquals(1, getTestTagsCount(finished.get(0)));
  }

  @Test
  public void test_two_callbacks() throws Exception {
    Thread entryThread = entryThreadWithTwoCallbacks();
    entryThread.start();
    entryThread.join(10_000);
    // Entry thread is completed but Callbacks are still running (or even not started)

    await().atMost(15, TimeUnit.SECONDS).until(reportedSpansSize(tracer), equalTo(1));

    List<MockSpan> finished = tracer.finishedSpans();
    assertEquals(1, finished.size());

    // Check that two callbacks finished and each added to span own tag ('test_tag_{random}')
    assertEquals(2, getTestTagsCount(finished.get(0)));
  }

  private int getTestTagsCount(MockSpan mockSpan) {
    Map<String, Object> tags = mockSpan.tags();
    int tagCounter = 0;
    for (String tagKey : tags.keySet()) {
      if (tagKey.startsWith("test_tag_")) {
        tagCounter++;
      }
    }
    return tagCounter;
  }

  /**
   * Thread will be completed before callback completed.
   */
  private Thread entryThread() {
    return new Thread(new Runnable() {
      @Override
      public void run() {
        logger.info("Entry thread started");

        try (ContinuableScope scope = (ContinuableScope)tracer.buildSpan("parent").startActive()) {
          Runnable callback = new Callback(scope);

          // Callback is executed at some unpredictable time and we are not able to check status of the callback
          service.schedule(callback, 500, TimeUnit.MILLISECONDS);
        }

        logger.info("Entry thread finished");
      }
    });
  }

  /**
   * Thread will be completed before callback completed.
   */
  private Thread entryThreadWithTwoCallbacks() {
    return new Thread(new Runnable() {
      @Override
      public void run() {
        logger.info("Entry thread 2x started");
        try (ContinuableScope scope = (ContinuableScope)tracer.buildSpan("parent").startActive()) {
          Runnable callback = new Callback(scope);
          Runnable callback2 = new Callback(scope);

          Random random = new Random();

          // Callbacks are executed at some unpredictable time
          service.schedule(callback, random.nextInt(1000) + 100, TimeUnit.MILLISECONDS);
          service.schedule(callback2, random.nextInt(1000) + 100, TimeUnit.MILLISECONDS);

        }
        logger.info("Entry thread 2x finished");
      }
    });
  }
}
