package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class VulnerableApplication {

    public static void main(String[] args) {
        new File("/tmp/app-files").mkdirs();
        SpringApplication.run(VulnerableApplication.class, args);
    }
}
