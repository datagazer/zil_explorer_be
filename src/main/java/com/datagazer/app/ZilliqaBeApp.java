package com.datagazer.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.datagazer")
public class ZilliqaBeApp {

    public static void main(String[] args) {
        SpringApplication.run(ZilliqaBeApp.class, args);
    }

}

