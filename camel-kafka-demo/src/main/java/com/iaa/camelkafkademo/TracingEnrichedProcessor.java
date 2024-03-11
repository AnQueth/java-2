package com.iaa.camelkafkademo;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;


public class TracingEnrichedProcessor implements Processor {

    @Override
   @WithSpan    
    public void process(Exchange exchange) throws Exception {
        // TODO Auto-generated method stub
       Span.current().addEvent("headerprocessing");       
    }

    

}
