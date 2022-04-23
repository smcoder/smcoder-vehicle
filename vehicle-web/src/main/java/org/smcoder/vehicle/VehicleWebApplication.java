package org.smcoder.vehicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class VehicleWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehicleWebApplication.class, args);
	}
}
