package AutoSpanAgent;

import java.util.HashMap;

import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;

public class Counters {

    static HashMap<String, LongUpDownCounter> counters = new HashMap<String, LongUpDownCounter>();

    public static void AddIncrement(String name) {

        name = "methodscount";
        if (counters.containsKey(name)) {
            counters.get(name).add(1);
        } else {

            Meter meter = io.opentelemetry.api.GlobalOpenTelemetry.getMeter("test");
            LongUpDownCounter counter = meter.upDownCounterBuilder(name)

                    .setDescription("method call counts")
                    .setUnit("number of calls")
                    .build();
            counter.add(1);
            counters.put(name, counter);
        }
    }

}