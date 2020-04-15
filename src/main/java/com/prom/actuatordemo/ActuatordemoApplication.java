package com.prom.actuatordemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ActuatordemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ActuatordemoApplication.class, args);
	}

//	@Bean
//	MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
//	  return registry -> registry.config().commonTags("user", "token1", "application", "actuator-demo");
//	}
}
