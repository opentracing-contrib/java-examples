package io.opentracing.contrib.examples.activate_deactivate;

import io.opentracing.Scope;
import io.opentracing.usecases.AutoFinishScopeManager;
import io.opentracing.usecases.AutoFinishScopeManager.AutoFinishScope;
import io.opentracing.tag.Tags;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback which executed at some unpredictable time. We don't know when it is started, when it is
 * completed. We cannot check status of it (started or completed)
 */
public class Callback implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(Callback.class);

  private final Random random = new Random();

  private final AutoFinishScope.Continuation continuation;

  Callback(Scope activeSpan) {
    continuation = ((AutoFinishScope)activeSpan).defer();
    logger.info("Callback created");
  }

  /**
   * Can be used continuation.activate().deactivate() chain only. It is splitted for testing
   * purposes (span should not be finished before deactivate() called here).
   */
  @Override
  public void run() {
    logger.info("Callback started");

    Scope scope = continuation.activate();
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // set random tag starting with 'test_tag_' to test that finished span has all of them
    scope.span().setTag("test_tag_" + random.nextInt(), "random");

    scope.close();
    logger.info("Callback finished");
  }
}
