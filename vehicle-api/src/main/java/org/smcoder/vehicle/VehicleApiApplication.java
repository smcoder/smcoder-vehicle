package org.smcoder.vehicle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
@MapperScan("org.smcoder.vehicle")
public class VehicleApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehicleApiApplication.class, args);
	}
}
