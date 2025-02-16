package com.mybank.balance.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableCaching
@SpringBootApplication(scanBasePackages = {"com.mybank.balance.transaction"})
public class RealtimeBalanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealtimeBalanceApplication.class, args);
	}

}
