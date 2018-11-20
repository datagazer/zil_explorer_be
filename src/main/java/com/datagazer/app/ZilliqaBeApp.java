package com.datagazer.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.datagazer")
@EnableScheduling
public class ZilliqaBeApp {

    public static void main(String[] args) {
        SpringApplication.run(ZilliqaBeApp.class, args);
    }

}

