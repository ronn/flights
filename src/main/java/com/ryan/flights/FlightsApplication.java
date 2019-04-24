package com.ryan.flights;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FlightsApplication {
	public static void main(String[] args) {
		BasicConfigurator.configure();
		SpringApplication.run(FlightsApplication.class, args);
	}
}