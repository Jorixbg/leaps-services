package com.leaps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.leaps.model.utils.LeapsUtils;

@SpringBootApplication
public class LeapsApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeapsApplication.class, args);
		
		// run the cron scheduler for creating repeating events
		LeapsUtils.runScheduler();
	}
}
