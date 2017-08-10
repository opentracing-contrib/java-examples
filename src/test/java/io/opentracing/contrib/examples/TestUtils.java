package io.opentracing.contrib.examples;

import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import java.util.concurrent.Callable;

public class TestUtils {

  public static Callable<Integer> reportedSpansSize(final MockTracer tracer) {
    return new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return tracer.finishedSpans().size();
      }
    };
  }

}
