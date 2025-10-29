package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import java.util.List;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.info.InfoContributorAutoConfiguration;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.info.OsInfoContributor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.info.AxileInfoEndpoint;
import com.nucleonforge.axile.sbs.spring.info.DefaultServiceInfoAssembler;
import com.nucleonforge.axile.sbs.spring.info.GitInformation;
import com.nucleonforge.axile.sbs.spring.info.KotlinVersionProvider;
import com.nucleonforge.axile.sbs.spring.info.ServiceInfoAssembler;
import com.nucleonforge.axile.sbs.spring.master.LibraryDiscoverer;

@AutoConfiguration(after = {InfoContributorAutoConfiguration.class, LibraryDiscovererAutoConfiguration.class})
@ConditionalOnAvailableEndpoint(endpoint = InfoEndpoint.class)
public class AxileInfoEndpointAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public GitInformation gitInformation(GitProperties gitProperties) {
        return new GitInformation(gitProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public KotlinVersionProvider kotlinVersionProvider() {
        return new KotlinVersionProvider();
    }

    @Bean
    public ServiceInfoAssembler serviceInfoAssembler(
            List<InfoContributor> infoContributors,
            GitInformation gitInformation,
            KotlinVersionProvider kotlinVersionProvider,
            LibraryDiscoverer libraryDiscoverer) {
        return new DefaultServiceInfoAssembler(
                infoContributors, gitInformation, kotlinVersionProvider, libraryDiscoverer);
    }

    @Bean
    @ConditionalOnBean({OsInfoContributor.class})
    @ConditionalOnMissingBean
    public AxileInfoEndpoint axileInfoEndpoint(ServiceInfoAssembler serviceInfoAssembler) {
        return new AxileInfoEndpoint(serviceInfoAssembler);
    }
}
