package io.opentracing.contrib.examples.listener_per_request;

import io.opentracing.Span;

/**
 * Response listener per request. Executed in a thread different from 'send' thread
 */
public class ResponseListener {

  private final Span span;

  public ResponseListener(Span span) {
    this.span = span;
  }

  /**
   * executed when response is received from server. Any thread.
   */
  public void onResponse(Object response) {
    span.finish();
  }
}
