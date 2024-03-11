package AutoSpanAgent;

import java.lang.reflect.Executable;
import java.util.HashMap;



import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import net.bytebuddy.asm.Advice;

class EnterAdvice {
  @Advice.OnMethodEnter
  static Wrapper enter(@Advice.Origin Executable method) {

    final long startTime = System.nanoTime();
    Span span = null;
     LongUpDownCounter counter = null;

    if (io.opentelemetry.context.Context.current() != null) {

      Tracer tracer = io.opentelemetry.api.GlobalOpenTelemetry.getTracer("test");
      final String name = method.getClass().getName() + "-" + method.getName() ;

      span = tracer.spanBuilder(name).setParent(io.opentelemetry.context.Context.current()).startSpan();
  
      Counters.AddIncrement(name);

    }

    return new Wrapper(span, startTime);
  }
}

