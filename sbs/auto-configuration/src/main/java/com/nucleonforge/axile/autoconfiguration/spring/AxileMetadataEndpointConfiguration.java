package com.nucleonforge.axile.autoconfiguration.spring;

import java.util.List;

import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.spring.master.AxileMetadataEndpoint;
import com.nucleonforge.axile.spring.master.CommitIdPluginGitInformationProvider;
import com.nucleonforge.axile.spring.master.CommitIdPluginShortBuildInfoProvider;
import com.nucleonforge.axile.spring.master.DefaultServiceMetadataAssembler;
import com.nucleonforge.axile.spring.master.GitInformationProvider;
import com.nucleonforge.axile.spring.master.LibraryDiscoverer;
import com.nucleonforge.axile.spring.master.ServiceMetadataAssembler;
import com.nucleonforge.axile.spring.master.ShortBuildInfoProvider;

/**
 * Auto-configuration for the {@link AxileMetadataEndpoint}.
 * <p>
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration(
        after = {
            HealthEndpointAutoConfiguration.class,
            CommitIdPluginGitInformationProvider.class,
            CommitIdPluginShortBuildInfoProvider.class,
            LibraryDiscovererAutoConfiguration.class
        })
public class AxileMetadataEndpointConfiguration {

    @Bean
    ServiceMetadataAssembler serviceMetadataAssembler(
            HealthEndpoint healthEndpoint,
            LibraryDiscoverer libraryDiscoverer,
            List<GitInformationProvider> gitInformationProviders,
            List<ShortBuildInfoProvider> shortBuildInfoProviders) {
        return new DefaultServiceMetadataAssembler(
                healthEndpoint, libraryDiscoverer, gitInformationProviders, shortBuildInfoProviders);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxileMetadataEndpoint axileMetadataEndpoint(ServiceMetadataAssembler serviceMetadataAssembler) {
        return new AxileMetadataEndpoint(serviceMetadataAssembler);
    }
}
