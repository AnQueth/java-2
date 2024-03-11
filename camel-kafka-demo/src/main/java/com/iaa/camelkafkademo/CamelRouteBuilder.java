package com.iaa.camelkafkademo;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.kafka.listener.ExceptionClassifier;
import org.springframework.stereotype.Component;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

@Component
public class CamelRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
       // from("timer:foo").to("log:bar");
       from("kafka:pageviews?brokers=localhost:9092")
       .process(new TracingEnrichedProcessor())
       .process(exchange -> {
         Tracer tracer = GlobalOpenTelemetry.getTracer("process-sf-to-reltio");
         Span mapperSpan = tracer.spanBuilder("results-mapper").startSpan();
        // Custom processing logic
         String body = exchange.getIn().getBody(String.class);
         String modifiedBody = "Processed: " + body;
         exchange.getIn().setBody(modifiedBody);
         mapperSpan.end();
        })
       .to("log:bar");

    }
}