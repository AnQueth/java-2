package kafkhasamplereceiver;

import org.apache.camel.Exchange;

@FunctionalInterface
public interface ExchangeRunnable {
    void run(Exchange exchange);
}