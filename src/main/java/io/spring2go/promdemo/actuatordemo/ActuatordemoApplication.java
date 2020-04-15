package io.spring2go.promdemo.actuatordemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

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
