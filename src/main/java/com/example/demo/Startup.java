package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Startup implements CommandLineRunner {
	
	@Autowired
	private Application application;

	public static void main(String[] args) {
//		SpringApplication.run(Startup.class, args);
		
		SpringApplication app = new SpringApplication(Startup.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
			application.run(args);
	}

}

