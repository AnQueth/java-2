package kafkhasample;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;

public class App {
 

    public static void main(String[] args) {

 
        try {

        CamelContext context = new DefaultCamelContext();

        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("timer://foo?period=50")
                    .setBody().simple("Hello World!")
                    .to("kafka:test?brokers=localhost:29092")
                    .process(exchange -> {
                        String message = exchange.getIn().getBody(String.class);
                        System.out.println("Sent message: " + message);
                    });

                    from("kafka:test?brokers=localhost:29092&groupId=group1")
                    .process(exchange -> {
                        String message = exchange.getIn().getBody(String.class);
                        System.out.println("Received message part 1: " + message);
                    }).to("kafka:another?brokers=localhost:29092");

                    from("kafka:another?brokers=localhost:29092&groupId=group2")
                    .process(exchange -> {
                        String message = exchange.getIn().getBody(String.class);
                        System.out.println("Received message part 2: " + message);
                    });
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
