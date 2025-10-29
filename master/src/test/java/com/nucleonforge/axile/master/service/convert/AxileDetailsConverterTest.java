package com.nucleonforge.axile.master.service.convert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.details.AxileDetails;
import com.nucleonforge.axile.common.api.details.TransitAxileDetails;
import com.nucleonforge.axile.common.api.details.components.BuildDetails;
import com.nucleonforge.axile.common.api.details.components.GitDetails;
import com.nucleonforge.axile.common.api.details.components.OsDetails;
import com.nucleonforge.axile.common.api.details.components.RuntimeDetails;
import com.nucleonforge.axile.common.api.details.components.SpringDetails;
import com.nucleonforge.axile.master.api.response.details.AxileDetailsResponse;
import com.nucleonforge.axile.master.api.response.details.components.BuildProfile;
import com.nucleonforge.axile.master.api.response.details.components.GitProfile;
import com.nucleonforge.axile.master.api.response.details.components.OSProfile;
import com.nucleonforge.axile.master.api.response.details.components.RuntimeProfile;
import com.nucleonforge.axile.master.api.response.details.components.SpringProfile;
import com.nucleonforge.axile.master.service.convert.details.AxileDetailsConverter;
import com.nucleonforge.axile.master.service.convert.details.components.BuildDetailsConverter;
import com.nucleonforge.axile.master.service.convert.details.components.GitDetailsConverter;
import com.nucleonforge.axile.master.service.convert.details.components.OSDetailsConverter;
import com.nucleonforge.axile.master.service.convert.details.components.RuntimeDetailsConverter;
import com.nucleonforge.axile.master.service.convert.details.components.SpringDetailsConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AxileDetailsConverter}
 *
 * @author Sergey Cherkasov
 */
public class AxileDetailsConverterTest {
    private AxileDetailsConverter converter;

    @BeforeEach
    void setUp() {
        converter = new AxileDetailsConverter(
                new GitDetailsConverter(),
                new RuntimeDetailsConverter(),
                new SpringDetailsConverter(),
                new BuildDetailsConverter(),
                new OSDetailsConverter());
    }

    @Test
    void testConvertHappyPath() {
        // when.
        AxileDetailsResponse response = converter.convertInternal(getAxileDetails());

        // ServiceName
        assertThat(response.serviceName()).isEqualTo("test");

        // GitProfile
        GitProfile git = response.git();
        assertThat(git.commitShaShort()).isEqualTo("7a663cb");
        assertThat(git.branch()).isEqualTo("local/local-test");
        assertThat(git.authorName()).isEqualTo("Никита");
        assertThat(git.authorEmail()).isEqualTo("145802687+NikitaKirilloff@users.noreply.github.com");
        assertThat(git.commitTimestamp()).isEqualTo("1761249922000");

        // SpringProfile
        SpringProfile spring = response.spring();
        assertThat(spring.springBootVersion()).isEqualTo("3.5.0");
        assertThat(spring.springFrameworkVersion()).isEqualTo("7.0");
        assertThat(spring.springCloudVersion()).isEqualTo("2013.0.8");

        // RuntimeProfile
        RuntimeProfile runtime = response.runtime();
        assertThat(runtime.javaVersion()).isEqualTo("17.0.16");
        assertThat(runtime.jdkVendor()).isEqualTo("Corretto-17.0.16.8.1");
        assertThat(runtime.garbageCollector()).isEqualTo("G1 GC");
        assertThat(runtime.kotlinVersion()).isEqualTo("1.9.0");

        // BuildProfile
        BuildProfile build = response.build();
        assertThat(build.artifact()).isEqualTo("spring-petclinic");
        assertThat(build.version()).isEqualTo("3.5.0-SNAPSHOT");
        assertThat(build.group()).isEqualTo("org.springframework.samples");
        assertThat(build.time()).isEqualTo("2025-10-29T15:10:54.770Z");

        // OSProfile
        OSProfile os = response.os();
        assertThat(os.name()).isEqualTo("Windows 10");
        assertThat(os.version()).isEqualTo("10.0");
        assertThat(os.arch()).isEqualTo("amd64");
    }

    private static TransitAxileDetails getAxileDetails() {
        // GitDetails.CommitAuthor
        GitDetails.CommitAuthor commitAuthor =
                new GitDetails.CommitAuthor("Никита", "145802687+NikitaKirilloff@users.noreply.github.com");

        // GitDetails
        GitDetails gitDetails = new GitDetails("7a663cb", "local/local-test", commitAuthor, "1761249922000");

        // SpringDetails
        SpringDetails springDetails = new SpringDetails("3.5.0", "7.0", "2013.0.8");

        // RuntimeDetails
        RuntimeDetails runtimeDetails = new RuntimeDetails("17.0.16", "Corretto-17.0.16.8.1", "G1 GC", "1.9.0");

        // BuildDetails
        BuildDetails buildDetails = new BuildDetails(
                "spring-petclinic", "3.5.0-SNAPSHOT", "org.springframework.samples", "2025-10-29T15:10:54.770Z");

        // OSDetails
        OsDetails osDetails = new OsDetails("Windows 10", "10.0", "amd64");

        // AxileDetails
        AxileDetails axileDetails =
                new AxileDetails(gitDetails, springDetails, runtimeDetails, buildDetails, osDetails);

        // ServiceAxileDetails
        return new TransitAxileDetails("test", axileDetails);
    }
}
