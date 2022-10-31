package com.fastcampus.projectboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.HiddenHttpMethodFilter;


@ConfigurationPropertiesScan
@SpringBootApplication
public class FastCampusProjectBoardApplication {

	public static void main(String[] args) {
		SpringApplication.run(FastCampusProjectBoardApplication.class, args);
	}
	@Bean
	public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		return new HiddenHttpMethodFilter();
	}
}
