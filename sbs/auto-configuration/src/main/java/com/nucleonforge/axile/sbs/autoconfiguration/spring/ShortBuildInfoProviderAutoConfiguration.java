package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.master.CommitIdPluginShortBuildInfoProvider;
import com.nucleonforge.axile.sbs.spring.master.GitInformationProvider;
import com.nucleonforge.axile.sbs.spring.master.NoOpGitInformationProvider;
import com.nucleonforge.axile.sbs.spring.master.NoOpShortBuildInfoProvider;
import com.nucleonforge.axile.sbs.spring.master.ShortBuildInfoProvider;

/**
 * Auto-Configuration for registering the appropriate {@link ShortBuildInfoProvider} instances.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration(after = ProjectInfoAutoConfiguration.class)
public class ShortBuildInfoProviderAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ShortBuildInfoProviderAutoConfiguration.class);

    @Bean
    @ConditionalOnBean(GitProperties.class)
    public ShortBuildInfoProvider commitIdPluginShortBuildInfoProvider(GitProperties gitProperties) {
        return new CommitIdPluginShortBuildInfoProvider(gitProperties);
    }

    @Bean
    @ConditionalOnMissingBean(ShortBuildInfoProvider.class)
    public ShortBuildInfoProvider noOpShortBuildInfoProvider() {
        log.warn(
                """
            The {} is active. It practically means that the build information (the version of the build,
            the timestamp when app was build) will not be determined. If you see this message,
            then we were not able to find any valid {} that is going to work in your setup.
            """,
                NoOpGitInformationProvider.class.getSimpleName(),
                GitInformationProvider.class.getSimpleName());
        return new NoOpShortBuildInfoProvider();
    }
}
