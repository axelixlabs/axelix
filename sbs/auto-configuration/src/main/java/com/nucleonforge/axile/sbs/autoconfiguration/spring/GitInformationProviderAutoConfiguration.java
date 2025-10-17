package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.master.CommitIdPluginGitInformationProvider;
import com.nucleonforge.axile.sbs.spring.master.GitInformationProvider;
import com.nucleonforge.axile.sbs.spring.master.NoOpGitInformationProvider;

/**
 * Auto-Configuration for registering the appropriate {@link GitInformationProvider} instances.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration(after = ProjectInfoAutoConfiguration.class)
public class GitInformationProviderAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GitInformationProviderAutoConfiguration.class);

    @Bean
    @ConditionalOnBean(GitProperties.class)
    public GitInformationProvider commitIdPluginGitInformationProvider(GitProperties gitProperties) {
        return new CommitIdPluginGitInformationProvider(gitProperties);
    }

    @Bean
    @ConditionalOnMissingBean(GitInformationProvider.class)
    public GitInformationProvider noOpGitInformationProvider() {
        log.warn(
                """
            The {} is active. It practically means that the git information (sha of last commit,
            the author of the last commit etc.) will not be determined. If you see this message,
            then we were not able to find any valid {} that is going to work in your setup.
            """,
                NoOpGitInformationProvider.class.getSimpleName(),
                GitInformationProvider.class.getSimpleName());
        return new NoOpGitInformationProvider();
    }
}
