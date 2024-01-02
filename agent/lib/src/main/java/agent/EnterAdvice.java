package agent;
import java.lang.reflect.Executable;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import net.bytebuddy.asm.Advice;
 
 class EnterAdvice {
      @Advice.OnMethodEnter
      static Wrapper enter(@Advice.Origin Executable method) {

        final long startTime = System.nanoTime();
        Span span = null;
 
    
        if(io.opentelemetry.context.Context.current() != null)
        {

                   Tracer tracer =io.opentelemetry.api.GlobalOpenTelemetry.getTracer("test");
          span = tracer.spanBuilder(  method.getName()   ).setParent(io.opentelemetry.context.Context.current()).startSpan();
             
        }
        
   
        return new Wrapper(span, startTime);
      }
    }


