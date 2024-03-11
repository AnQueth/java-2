package kafkhasample2;

import org.apache.camel.Exchange;

@FunctionalInterface
public interface ExchangeRunnable {
    void run(Exchange exchange);
}