package com.nucleonforge.axile.autoconfiguration.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.spring.exception.ActuatorExceptionHandler;

@AutoConfiguration
public class SpringActuatorExceptionHandlerAutoConfiguration {

    @Bean
    public ActuatorExceptionHandler actuatorExceptionHandler() {
        return new ActuatorExceptionHandler();
    }
}
