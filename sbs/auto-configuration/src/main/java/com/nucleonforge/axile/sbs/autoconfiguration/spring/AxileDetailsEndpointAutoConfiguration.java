/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.info.InfoContributorAutoConfiguration;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.details.AxileDetailsEndpoint;
import com.nucleonforge.axile.sbs.spring.details.DefaultServiceDetailsAssembler;
import com.nucleonforge.axile.sbs.spring.details.ServiceDetailsAssembler;
import com.nucleonforge.axile.sbs.spring.master.GitInformationProvider;
import com.nucleonforge.axile.sbs.spring.master.LibraryDiscoverer;

/**
 * Auto-configuration for the {@link AxileDetailsEndpoint}.
 *
 * @since 30.10.2025
 * @author Nikita Kirillov, Sergey Cherkasov
 */
@AutoConfiguration(
        after = {
            InfoContributorAutoConfiguration.class,
            LibraryDiscovererAutoConfiguration.class,
            GitInformationProviderAutoConfiguration.class
        })
@ConditionalOnAvailableEndpoint(endpoint = InfoEndpoint.class)
public class AxileDetailsEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AxileDetailsEndpoint axileDetailsEndpoint(ServiceDetailsAssembler serviceDetailsAssembler) {
        return new AxileDetailsEndpoint(serviceDetailsAssembler);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceDetailsAssembler serviceInfoAssembler(
            GitInformationProvider gitInformationProvider,
            ObjectProvider<BuildProperties> providerBuildProperties,
            LibraryDiscoverer libraryDiscoverer) {
        return new DefaultServiceDetailsAssembler(gitInformationProvider, providerBuildProperties, libraryDiscoverer);
    }
}
