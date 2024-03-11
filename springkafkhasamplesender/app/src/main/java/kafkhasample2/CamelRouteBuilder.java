package kafkhasample2;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

@Component
public class CamelRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("timer://foo?period=1000")
                .setBody().simple("Hello World!")
                .process(ReadSpanFromHeader())

                .to("kafka:test?brokers=localhost:29092")
                .process(exchange -> {

                    runInNewSpan(GlobalOpenTelemetry.getTracer("testing"), "span sent message", e -> {
                        String message = e.getIn().getBody(String.class);
                        System.out.println("Sent message: " + message);

                    }, exchange);

                });

        from("kafka:test?brokers=localhost:29092&groupId=group1")
                .process(ReadSpanFromHeader())
                .process(exchange -> {

                    runInNewSpan(GlobalOpenTelemetry.getTracer("testing"), "span received message", e -> {
                        String message = e.getIn().getBody(String.class);
                        System.out.println("received  message part 1: " + message);

                    }, exchange);
                })
                .process(SetTraceInHeader())

                .to("kafka:another?brokers=localhost:29092");
    }


    public void runInNewSpan(Tracer tracer, String spanName, ExchangeRunnable function, Exchange exchange) {
        Span parentSpan = Span.current();
        Span span = tracer.spanBuilder(spanName).setParent(Context.current().with(parentSpan)).startSpan();
        try (Scope scope = span.makeCurrent()) {
            function.run(exchange);
        } finally {
            span.end();
        }
    }
    private Processor ReadSpanFromHeader() {
        return exchange -> {
            String traceparent = exchange.getIn().getHeader("traceparent", String.class);
            if (traceparent != null) {
                String[] parts = traceparent.split("-");
                String traceId = parts[1];
                String spanId = parts[2];
                SpanContext parentContext = SpanContext.createFromRemoteParent(
                        traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault());

                Span span = GlobalOpenTelemetry.getTracer("testing")
                        .spanBuilder("span-name")
                        .setParent(Context.current().with(Span.wrap(parentContext)))
                        .startSpan();

                span.makeCurrent();
            }

        };
    }

    private Processor SetTraceInHeader() {
        return exchange -> {
            Span currentSpan = Span.current();

            // Get the span context
            SpanContext spanContext = currentSpan.getSpanContext();

            String traceId = spanContext.getTraceId();
            TraceState traceState = spanContext.getTraceState();

            // Set the traceparent header
            Message in = exchange.getIn();
            in.setHeader("traceparent", "00-" + traceId + "-" + spanContext.getSpanId() + "-01");

        };
    }

}