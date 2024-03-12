package kafkhasample2;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.opentelemetry.OpenTelemetryTracer;
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
                .process(SetTraceInHeader())

                .to("kafka:test?brokers=localhost:29092")

                .process(exchange -> {

                    runInNewSpan(GlobalOpenTelemetry.getTracer("testing"), "span sent message", e -> {
                        String message = exchange.getIn().getBody(String.class);
                        System.out.println("Sent message: " + message);

                    }, exchange);

                });

        from("kafka:test?brokers=localhost:29092&groupId=group1")

                .process(ReadSpanFromHeader())
                .process(exchange -> {

                    runInNewSpan(GlobalOpenTelemetry.getTracer("testing"), "span received message", e -> {
                        String message = exchange.getIn().getBody(String.class);
                        System.out.println("received  message part 1: " + message);

                    }, exchange);
                })
                .process(SetTraceInHeader())

                .to("kafka:another?brokers=localhost:29092");

    }

    public void StartNewOverallSpan(Tracer tracer, String spanName, Exchange exchange) {
        Span span = tracer.spanBuilder(spanName).startSpan();
        span.makeCurrent();

        exchange.getMessage().setHeader("currentspan", span);

    }

    public void EndOverallSpan(Exchange exchange) {
        Span span = exchange.getMessage().getHeader("currentspan", Span.class);
        if (span != null)
            span.end();
    }

    public void runInNewSpan(Tracer tracer, String spanName, ExchangeRunnable function, Exchange exchange) {

        Span cs = exchange.getMessage().getHeader("currentspan", Span.class);
        Span toUse = null;
        if (cs != null) {
            toUse = tracer.spanBuilder(spanName).setParent(Context.current().with(cs)).startSpan();
        } else {
            Span parentSpan = Span.current();
            toUse = tracer.spanBuilder(spanName).setParent(Context.current().with(parentSpan)).startSpan();

        }

        try (Scope scope = toUse.makeCurrent()) {
            function.run(exchange);
        } finally {
            toUse.end();
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
                exchange.getMessage().setHeader("currentspan", span);

            }

        };
    }

    private Processor SetTraceInHeader() {
        return exchange -> {
            Span currentSpan = Span.current();

            // Get the span context
            SpanContext spanContext = currentSpan.getSpanContext();

            String traceId = spanContext.getTraceId();

            // Set the traceparent header
            Message in = exchange.getIn();
            in.setHeader("traceparent", "00-" + traceId + "-" + spanContext.getSpanId() + "-01");

        };
    }

}