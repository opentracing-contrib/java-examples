package io.opentracing.contrib.examples.common_request_handler;

import io.opentracing.contrib.examples.TestUtils;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {

  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  private final ExecutorService executor = Executors.newCachedThreadPool();

  private final RequestHandler requestHandler;


  public Client(RequestHandler requestHandler) {
    this.requestHandler = requestHandler;
  }


  public Future<Object> send(final Object message) {

    final Context context = new Context();
    return executor.submit(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        logger.info("send {}", message);
        TestUtils.sleep();
        executor.submit(new Runnable() {
          @Override
          public void run() {
            TestUtils.sleep();
            requestHandler.beforeRequest(message, context);
          }
        }).get();

        executor.submit(new Runnable() {
          @Override
          public void run() {
            TestUtils.sleep();
            requestHandler.afterResponse(message, context);
          }
        }).get();

        return message + ":response";
      }
    });

  }
}
