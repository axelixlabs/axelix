package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.info.InfoContributorAutoConfiguration;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.info.OsInfoContributor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.details.AxileDetailsEndpoint;
import com.nucleonforge.axile.sbs.spring.details.DefaultServiceDetailsAssembler;
import com.nucleonforge.axile.sbs.spring.details.ServiceDetailsAssembler;
import com.nucleonforge.axile.sbs.spring.master.GitInformationProvider;
import com.nucleonforge.axile.sbs.spring.master.LibraryDiscoverer;

@AutoConfiguration(after = {InfoContributorAutoConfiguration.class, LibraryDiscovererAutoConfiguration.class})
@ConditionalOnAvailableEndpoint(endpoint = InfoEndpoint.class)
public class AxileDetailsEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceDetailsAssembler serviceInfoAssembler(
            GitInformationProvider gitInformationProvider,
            ObjectProvider<BuildProperties> providerBuildProperties,
            LibraryDiscoverer libraryDiscoverer) {
        return new DefaultServiceDetailsAssembler(gitInformationProvider, providerBuildProperties, libraryDiscoverer);
    }

    @Bean
    @ConditionalOnBean({OsInfoContributor.class})
    @ConditionalOnMissingBean
    public AxileDetailsEndpoint axileDetailsEndpoint(ServiceDetailsAssembler serviceDetailsAssembler) {
        return new AxileDetailsEndpoint(serviceDetailsAssembler);
    }
}
