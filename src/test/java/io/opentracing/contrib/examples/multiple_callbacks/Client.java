package io.opentracing.contrib.examples.multiple_callbacks;

import static io.opentracing.contrib.examples.TestUtils.sleep;

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Client {

  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final Tracer tracer;

  public Client(Tracer tracer) {
    this.tracer = tracer;
  }

  public void send(final Object message, ActiveSpan parentSpan, final long milliseconds) {
    final ActiveSpan.Continuation cont = parentSpan.capture();
    executor.submit(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        logger.info("Child thread with message '" + message + "' started");

        try (ActiveSpan parentSpan = cont.activate()) {
          try (ActiveSpan span = tracer.buildSpan("subtask").startActive()) {
            // Simulate work.
            sleep(milliseconds);
          }
        }

        logger.info("Child thread with message '" + message + "' finished");
        return message + "::response";
      }
    });
  }
}
