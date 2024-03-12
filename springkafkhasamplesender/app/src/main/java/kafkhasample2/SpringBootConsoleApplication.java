
package kafkhasample2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import org.springframework.boot.CommandLineRunner;

 
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
 
            Thread.sleep(100000);
     
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
