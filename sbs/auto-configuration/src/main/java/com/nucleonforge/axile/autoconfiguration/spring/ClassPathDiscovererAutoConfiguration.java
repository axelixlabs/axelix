package com.nucleonforge.axile.autoconfiguration.spring;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.spring.build.FlatClassPathDiscoverer;
import com.nucleonforge.axile.spring.build.FlatGradleMetadataExtractor;
import com.nucleonforge.axile.spring.build.FlatMavenMetadataExtractor;
import com.nucleonforge.axile.spring.build.JarMetadataExtractor;

/**
 * Spring Boot auto-configuration for JAR metadata discovery.
 *
 * @since 20.08.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class ClassPathDiscovererAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(FlatMavenMetadataExtractor.class)
    public FlatMavenMetadataExtractor flatMavenMetadataExtractor() {
        return new FlatMavenMetadataExtractor();
    }

    @Bean
    @ConditionalOnMissingBean(FlatGradleMetadataExtractor.class)
    public FlatGradleMetadataExtractor flatGradleMetadataExtractor() {
        return new FlatGradleMetadataExtractor();
    }

    @Bean
    @ConditionalOnMissingBean(FlatClassPathDiscoverer.class)
    public FlatClassPathDiscoverer flatClassPathDiscoverer(List<JarMetadataExtractor> extractors) {
        return new FlatClassPathDiscoverer(extractors);
    }
}
