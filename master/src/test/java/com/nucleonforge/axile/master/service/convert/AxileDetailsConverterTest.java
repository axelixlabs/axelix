package com.nucleonforge.axile.master.service.convert;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.AxileDetails;
import com.nucleonforge.axile.master.api.response.AxileDetailsResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AxileDetailsConverter}
 *
 * @author Sergey Cherkasov
 */
public class AxileDetailsConverterTest {
    private final AxileDetailsConverter converter = new AxileDetailsConverter();

    @Test
    void testConvertHappyPath() {
        // when.
        AxileDetailsResponse response = converter.convertInternal(getAxileDetails());

        // ServiceName
        assertThat(response.serviceName()).isEqualTo("test");

        // GitProfile
        AxileDetailsResponse.GitProfile git = response.git();
        assertThat(git.commitShaShort()).isEqualTo("7a663cb");
        assertThat(git.branch()).isEqualTo("local/local-test");
        assertThat(git.authorName()).isEqualTo("sergeycherkasovv");
        assertThat(git.authorEmail()).isEqualTo("sergeycherkasovv@github.com");
        assertThat(git.commitTimestamp()).isEqualTo("1761249922000");

        // SpringProfile
        AxileDetailsResponse.SpringProfile spring = response.spring();
        assertThat(spring.springBootVersion()).isEqualTo("3.5.0");
        assertThat(spring.springFrameworkVersion()).isEqualTo("7.0");
        assertThat(spring.springCloudVersion()).isEqualTo("2013.0.8");

        // RuntimeProfile
        AxileDetailsResponse.RuntimeProfile runtime = response.runtime();
        assertThat(runtime.javaVersion()).isEqualTo("17.0.16");
        assertThat(runtime.jdkVendor()).isEqualTo("Corretto-17.0.16.8.1");
        assertThat(runtime.garbageCollector()).isEqualTo("G1 GC");
        assertThat(runtime.kotlinVersion()).isEqualTo("1.9.0");

        // BuildProfile
        AxileDetailsResponse.BuildProfile build = response.build();
        assertThat(build.artifact()).isEqualTo("spring-petclinic");
        assertThat(build.version()).isEqualTo("3.5.0-SNAPSHOT");
        assertThat(build.group()).isEqualTo("org.springframework.samples");
        assertThat(build.time()).isEqualTo("2025-10-29T15:10:54.770Z");

        // OSProfile
        AxileDetailsResponse.OSProfile os = response.os();
        assertThat(os.name()).isEqualTo("Windows 10");
        assertThat(os.version()).isEqualTo("10.0");
        assertThat(os.arch()).isEqualTo("amd64");
    }

    private static AxileDetails getAxileDetails() {
        // GitDetails.CommitAuthor
        AxileDetails.GitDetails.CommitAuthor commitAuthor =
                new AxileDetails.GitDetails.CommitAuthor("sergeycherkasovv", "sergeycherkasovv@github.com");

        // GitDetails
        AxileDetails.GitDetails gitDetails =
                new AxileDetails.GitDetails("7a663cb", "local/local-test", commitAuthor, "1761249922000");

        // SpringDetails
        AxileDetails.SpringDetails springDetails = new AxileDetails.SpringDetails("3.5.0", "7.0", "2013.0.8");

        // RuntimeDetails
        AxileDetails.RuntimeDetails runtimeDetails =
                new AxileDetails.RuntimeDetails("17.0.16", "Corretto-17.0.16.8.1", "G1 GC", "1.9.0");

        // BuildDetails
        AxileDetails.BuildDetails buildDetails = new AxileDetails.BuildDetails(
                "spring-petclinic", "3.5.0-SNAPSHOT", "org.springframework.samples", "2025-10-29T15:10:54.770Z");

        // OSDetails
        AxileDetails.OsDetails osDetails = new AxileDetails.OsDetails("Windows 10", "10.0", "amd64");

        // return -> AxileDetails
        return new AxileDetails("test", gitDetails, springDetails, runtimeDetails, buildDetails, osDetails);
    }
}
