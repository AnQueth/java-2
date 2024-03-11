package AutoSpanAgent;

import java.lang.reflect.Executable;

import io.opentelemetry.api.trace.StatusCode;
import net.bytebuddy.asm.Advice;

class ExitAdviceMethods {
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    static void exit(@Advice.Origin final Executable executable,
            @Advice.Enter final Wrapper wrapper,
            @Advice.Thrown final Throwable throwable) {
        final long duration = System.nanoTime() - wrapper.nanos;

        if (wrapper.span != null) 
        {
            wrapper.span.end();
            if (throwable != null)
            {
                wrapper.span.recordException(throwable);
                wrapper.span.setStatus(StatusCode.ERROR);
            }
        }

        System.out.format("Time nanos for %s , %d\n", executable.getName(), duration);

    }
}
