package com.sivalabs.ft.features;

import org.springframework.boot.SpringApplication;

public class TestFeatureServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(FeatureServiceApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
