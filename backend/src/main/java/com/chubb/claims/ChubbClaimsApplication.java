package com.chubb.claims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ChubbClaimsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChubbClaimsApplication.class, args);
    }
}
