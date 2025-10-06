package com.nucleonforge.axile.autoconfiguration.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import com.nucleonforge.axile.spring.master.CycloneDXSBOMLibraryDiscoverer;
import com.nucleonforge.axile.spring.master.LibraryDiscoverer;
import com.nucleonforge.axile.spring.master.NoOpLibraryDiscoverer;

/**
 * Auto-Configuration for registering the appropriate {@link LibraryDiscoverer} instances.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
public class LibraryDiscovererAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LibraryDiscovererAutoConfiguration.class);

    @Bean
    @ConditionalOnCycloneDx
    public LibraryDiscoverer libraryDiscoverer() {
        return new CycloneDXSBOMLibraryDiscoverer();
    }

    @Bean
    @ConditionalOnMissingBean(LibraryDiscoverer.class)
    public LibraryDiscoverer noOpLibraryDiscoverer() {
        log.warn(
                """
            The {} is active. It practically means that the versions of libs such
            as spring-boot and others will not be determined. If you see this message,
            then we were not able to find any valid {} that is going to work in your setup.
            """,
                NoOpLibraryDiscoverer.class.getSimpleName(),
                LibraryDiscoverer.class.getSimpleName());
        return new NoOpLibraryDiscoverer();
    }

    // TODO: we might want to extract these into a top-level annotation in the future.
    /**
     * {@link Conditional} Spring annotation ot enable bean registration only in case the
     * CycloneDX infrastructure is in place.
     *
     * @author Mikhail Polivakha
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Conditional(value = ConditionOnCycloneDx.class)
    @interface ConditionalOnCycloneDx {}

    static class ConditionOnCycloneDx extends AnyNestedCondition {

        public ConditionOnCycloneDx() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnResource(resources = "classpath:META-INF/sbom/application.cdx.json")
        static class OnSbomResourceInClassPath {}

        // TODO:
        //  Again, we're not exactly sure about the shape of our properties and their prefixes.
        //  We're going to return to that in the future.
        @ConditionalOnProperty(prefix = "axile.sbom.cyclonedx", value = "location")
        static class OnExplicitPathToCycloneDXSbom {}
    }
}
