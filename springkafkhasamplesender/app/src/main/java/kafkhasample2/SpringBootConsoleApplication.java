
package kafkhasample2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;

import org.springframework.boot.CommandLineRunner;
import org.apache.camel.CamelContext;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.opentelemetry.OpenTelemetryTracer;
import org.apache.camel.opentelemetry.starter.CamelOpenTelemetry;
import org.apache.camel.opentelemetry.OpenTelemetryTracer;


@SpringBootApplication
@CamelOpenTelemetry
public class SpringBootConsoleApplication implements CommandLineRunner {
    private static Logger LOG = LoggerFactory.getLogger(SpringBootConsoleApplication.class);

    public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(SpringBootConsoleApplication.class, args);
        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) {
    
        try {

            CamelContext context = new DefaultCamelContext();
 
            context.addRoutes(new RouteBuilder() {
                public void configure() {
                    from("timer://foo?period=50")
                        .setBody().simple("Hello World!")
                        .process(  exchange ->
                        {
                             Span currentSpan = Span.current();

                // Get the span context
                SpanContext spanContext = currentSpan.getSpanContext();

                String traceId = spanContext.getTraceId();
                TraceState traceState = spanContext.getTraceState();

                // Set the traceparent header
                Message in = exchange.getIn();
                in.setHeader("traceparent", "00-" + traceId + "-" + spanContext.getSpanId() + "-01");


         
 
                          
                             
                            
                        })
                                            
                        .to("kafka:test?brokers=localhost:29092")
                        .process(exchange -> {
                            String message = exchange.getIn().getBody(String.class);
                            System.out.println("Sent message: " + message);
                        });
    
                        from("kafka:test?brokers=localhost:29092&groupId=group1")
                        .process(exchange -> {
                   String traceparent=exchange.getIn().getHeader("traceparent", String.class);
                if(traceparent != null) {
                    String[] parts = traceparent.split("-");
                    String traceId=parts[1];
                    String spanId=parts[2];
                    SpanContext parentContext=SpanContext.createFromRemoteParent(
                        traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault());

                    
                    
                    Span span=GlobalOpenTelemetry.getTracer("testing")
                        .spanBuilder("span-name")
                        .setParent(Context.current().with(Span.wrap(parentContext)))
                        .startSpan();
                        
                    span.makeCurrent();
                }

                        })
                        .process(exchange -> {


                            
                            String message = exchange.getIn().getBody(String.class);
                            System.out.println("Received message part 1: " + message);
                        })
                        .process(  exchange ->
                        {
                             Span currentSpan = Span.current();

                // Get the span context
                SpanContext spanContext = currentSpan.getSpanContext();

                String traceId = spanContext.getTraceId();
                TraceState traceState = spanContext.getTraceState();

                // Set the traceparent header
                Message in = exchange.getIn();
                in.setHeader("traceparent", "00-" + traceId + "-" + spanContext.getSpanId() + "-01");
 
                          
                             
                            
                        })
                        
                        .to("kafka:another?brokers=localhost:29092");
    
                }
            });
    
            context.start();
            Thread.sleep(100000);
            context.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
