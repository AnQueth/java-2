
package kafkhasamplereceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import org.springframework.boot.CommandLineRunner;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import org.apache.camel.opentelemetry.starter.CamelOpenTelemetry;

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
        
    
                        from("kafka:another?brokers=localhost:29092&groupId=group3")
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
                            String spanid = Span.current().getSpanContext().getTraceId();
                            String message = exchange.getIn().getBody(String.class);
                            System.out.println("Received message part 2: " + message + " " + spanid);
                        });
                       /* .process(exchange -> {


                            String spanid = Span.current().getSpanContext().getTraceId();
                      
                            System.out.println("spid: " + spanid);

                            Span newSpan = GlobalOpenTelemetry.getTracer("testing")
                            .spanBuilder("doing more work")
                            .startSpan();
        
                        try (Scope scope = newSpan.makeCurrent()) {
                            Thread.sleep(1000);
                            spanid = Span.current().getSpanContext().getTraceId();
                            String message = exchange.getIn().getBody(String.class);
                            System.out.println("Received message part 2: " + message + " " + spanid);
                        } finally {
                            newSpan.end();
                        }

         
                        });
                        */
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
