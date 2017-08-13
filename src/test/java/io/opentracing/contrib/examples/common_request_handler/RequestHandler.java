package io.opentracing.contrib.examples.common_request_handler;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One instance per Client. Executed concurrently for all requests of one client. 'beforeRequest'
 * and 'afterResponse' are executed in different threads for one 'send'
 */
public class RequestHandler {

  private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

  private final Tracer tracer;

  public RequestHandler(Tracer tracer) {
    this.tracer = tracer;
  }

  public void beforeRequest(Object request, Context context) {
    logger.info("before send {}", request);

    // we cannot use active span because we don't know in which thread it is executed
    // and we cannot therefore activate span. thread can come from common thread pool.
    Span span = tracer.buildSpan("send")
        .ignoreActiveSpan()
        .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
        .startManual();

    context.put("span", span);
  }

  public void afterResponse(Object response, Context context) {
    logger.info("after response {}", response);

    Object spanObject = context.get("span");
    if (spanObject != null && spanObject instanceof Span) {
      Span span = (Span) spanObject;
      span.finish();
    }
  }
}
