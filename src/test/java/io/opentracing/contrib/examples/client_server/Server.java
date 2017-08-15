package io.opentracing.contrib.examples.client_server;

import io.opentracing.ActiveSpan;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.tag.Tags;
import java.util.concurrent.ArrayBlockingQueue;

public class Server extends Thread {

  private final ArrayBlockingQueue<Message> queue;
  private final Tracer tracer;

  public Server(ArrayBlockingQueue<Message> queue, Tracer tracer) {
    this.queue = queue;
    this.tracer = tracer;
  }

  private void process(Message message) {
    SpanContext context = tracer.extract(Builtin.TEXT_MAP, new TextMapExtractAdapter(message));
    try (ActiveSpan activeSpan = tracer.buildSpan("receive").asChildOf(context).startActive()) {
      activeSpan.setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
    }
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {

      Message message;
      try {
        message = this.queue.take();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }

      process(message);
    }
  }
}
