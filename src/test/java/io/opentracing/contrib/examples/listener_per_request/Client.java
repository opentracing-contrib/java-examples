package io.opentracing.contrib.examples.listener_per_request;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Client {

  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final Tracer tracer;

  public Client(Tracer tracer) {
    this.tracer = tracer;
  }


  /**
   * Async execution
   */
  private Future<Object> execute(final Object message, final ResponseListener responseListener) {
    return executor.submit(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        // send via wire and get response
        Object response = message + ":response";
        responseListener.onResponse(response);
        return response;
      }
    });
  }

  public Future<Object> send(final Object message) {
    Span span = tracer.buildSpan("send").
        withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
        .startManual();
    return execute(message, new ResponseListener(span));
  }
}
