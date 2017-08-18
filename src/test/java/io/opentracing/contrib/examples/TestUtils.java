package io.opentracing.contrib.examples;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.AbstractTag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

  public static MockSpan getOneByTag(List<MockSpan> spans, AbstractTag key, Object value) {
    List<MockSpan> found = new ArrayList<>();
    for (MockSpan span : spans) {
      if (span.tags().get(key.getKey()).equals(value)) {
        found.add(span);
      }
    }
    if (found.size() > 1) {
      throw new RuntimeException("Ups, it's too much");
    }
    return found.isEmpty() ? null : found.get(0);
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

  public static void sortByStartMicros(List<MockSpan> spans) {
    Collections.sort(spans, new Comparator<MockSpan>() {
      @Override
      public int compare(MockSpan o1, MockSpan o2) {
        return Long.compare(o1.startMicros(), o2.startMicros());
      }
    });
  }
}
