package io.opentracing.contrib.examples.activate_deactivate;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpan.Continuation;
import io.opentracing.tag.Tags;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback which executed at some unpredictable time. We don't know when it is started, when it is
 * completed. We cannot check status of it (started or completed)
 */
public class Callback implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(Callback.class);

  private final Continuation continuation;

  Callback(ActiveSpan activeSpan) {
    continuation = activeSpan.capture();
    logger.info("Callback created");
  }

  @Override
  public void run() {
    logger.info("Callback started");
    ActiveSpan activeSpan = continuation.activate();

    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    activeSpan
        .setTag(Tags.HTTP_STATUS.getKey(), 200); // we need it to test that finished span has it
    activeSpan.deactivate();
    logger.info("Callback finished");
  }
}
