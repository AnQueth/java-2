package com.iaa.camelkafkademo;

import org.apache.camel.opentelemetry.starter.CamelOpenTelemetry;
import org.apache.camel.spring.Main;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;

@SpringBootApplication
@CamelOpenTelemetry
public class CamelKafkaDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CamelKafkaDemoApplication.class, args);	
	
	}

	

}
