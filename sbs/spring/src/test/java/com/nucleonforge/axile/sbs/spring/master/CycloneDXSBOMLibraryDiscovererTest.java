package com.nucleonforge.axile.sbs.spring.master;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;

/**
 * Unit test for {@link CycloneDXSBOMLibraryDiscoverer}.
 *
 * @author Mikhail Polivakha
 */
class CycloneDXSBOMLibraryDiscovererTest {

    private CycloneDXSBOMLibraryDiscoverer subject;

    @BeforeEach
    void setUp() {
        subject = new CycloneDXSBOMLibraryDiscoverer(new ClassPathResource("other/application.cdx.json"));
    }

    @Test
    void shouldFindSpringBootVersion() {
        // when
        Optional<String> libraryVersion = subject.getLibraryVersion("spring-boot", "org.springframework.boot");

        // then
        Assertions.assertThat(libraryVersion).isPresent().hasValue("3.5.0");
    }

    @Test
    void shouldReturnEmptyOptionalOnNonExistentLibrary() {
        // when
        Optional<String> libraryVersion = subject.getLibraryVersion("some-artifact", "some-group");

        // then
        Assertions.assertThat(libraryVersion).isEmpty();
    }
}
