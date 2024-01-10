package AutoSpanAgent;


import io.opentelemetry.api.trace.Span;
 

public class Wrapper {
    public final Span span;
    public final long nanos;


    public  Wrapper(Span span, long nanos) {
        this.span = span;
        this.nanos = nanos;

    }
}