//package io.opentracing.contrib.examples.multiple_callbacks;
//
//import static io.opentracing.contrib.examples.TestUtils.sleep;
//
//import io.opentracing.Scope;
//import io.opentracing.Span;
//import io.opentracing.Tracer;
//import io.opentracing.usecases.AutoFinishScopeManager.AutoFinishScope;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import org.slf4j.LoggerFactory;
//import org.slf4j.Logger;
//
//public class Client {
//
//  private static final Logger logger = LoggerFactory.getLogger(Client.class);
//
//  private final ExecutorService executor = Executors.newCachedThreadPool();
//  private final Tracer tracer;
//
//  public Client(Tracer tracer) {
//    this.tracer = tracer;
//  }
//
//  public Future<Object> send(final Object message, final Scope parentScope, final long milliseconds) {
//    final AutoFinishScope.Continuation cont = ((AutoFinishScope)parentScope).defer();
//
//    return executor.submit(new Callable<Object>() {
//      @Override
//      public Object call() throws Exception {
//        logger.info("Child thread with message '{}' started", message);
//
//        try (Scope parentScope = cont.activate()) {
//          try (Scope scope = tracer.buildSpan("subtask").startActive()) {
//            // Simulate work.
//            sleep(milliseconds);
//          }
//        }
//
//        logger.info("Child thread with message '{}' finished", message);
//        return message + "::response";
//      }
//    });
//  }
//}
