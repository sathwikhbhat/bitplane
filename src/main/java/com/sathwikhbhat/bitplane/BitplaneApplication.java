package com.sathwikhbhat.bitplane;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BitplaneApplication {

	public static void main(String[] args) {
		SpringApplication.run(BitplaneApplication.class, args);
	}

}
