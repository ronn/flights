package com.ryan.flights;

import com.fasterxml.jackson.databind.Module;
import io.vavr.jackson.datatype.VavrModule;
import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableCaching
public class FlightsApplication {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		SpringApplication.run(FlightsApplication.class, args);
	}

	@Bean
	Module vavrModule(){
		return new VavrModule();
	}
}