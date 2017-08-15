package io.opentracing.contrib.examples;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.AbstractTag;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class TestUtils {

  public static Callable<Integer> reportedSpansSize(final MockTracer tracer) {
    return new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return tracer.finishedSpans().size();
      }
    };
  }

  public static MockSpan getByTag(List<MockSpan> spans, AbstractTag key, Object value) {
    for (MockSpan span : spans) {
      if (span.tags().get(key.getKey()).equals(value)) {
        return span;
      }
    }
    return null;
  }

  public static void sleep() {
    try {
      TimeUnit.MILLISECONDS.sleep(new Random().nextInt(2000));
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
  }

  public static void sleep(long milliseconds) {
    try {
      TimeUnit.MILLISECONDS.sleep(milliseconds);
    } catch (InterruptedException e) {
        e.printStackTrace();
        Thread.currentThread().interrupt();
    }
  }
}
