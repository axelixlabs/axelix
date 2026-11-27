/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.service.ecosystem.platform;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.axelixlabs.axelix.master.domain.ecosystem.platform.Platform;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformReleaseLine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PlatformManifestLoader}.
 *
 * @author Mikhail Polivakha
 */
class PlatformManifestLoaderTest {

    @Test
    void loadsManifestsInFilenameOrderMappingEveryField() {
        // given
        PlatformManifestLoader subject = loaderOf("classpath*:axelix/platforms/valid/*.yaml");

        // when
        List<Platform> platforms = subject.load();

        // then
        assertThat(platforms).hasSize(2);

        Platform corretto = platforms.get(0);
        assertThat(corretto.name()).isEqualTo(PlatformName.AMAZON_CORRETTO);
        assertThat(corretto.lines())
                .containsExactly(
                        new PlatformReleaseLine("21", LocalDate.of(2023, 9, 21), LocalDate.of(2030, 10, 31), null));

        Platform springBoot = platforms.get(1);
        assertThat(springBoot.name()).isEqualTo(PlatformName.SPRING_BOOT);
        assertThat(springBoot.lines())
                .containsExactly(
                        new PlatformReleaseLine(
                                "3.2.x",
                                LocalDate.of(2023, 11, 23),
                                LocalDate.of(2024, 12, 31),
                                LocalDate.of(2025, 12, 31)),
                        new PlatformReleaseLine(
                                "3.3.x",
                                LocalDate.of(2024, 5, 23),
                                LocalDate.of(2025, 6, 30),
                                LocalDate.of(2026, 6, 30)));
    }

    @Test
    void refusesManifestWithUnknownProperty() {
        // given
        PlatformManifestLoader subject = loaderOf("classpath:axelix/platforms/broken/typo.yaml");

        // when / then
        assertThatThrownBy(subject::load)
                .isInstanceOf(PlatformCatalogException.class)
                .hasMessageContaining("Failed to read the platform manifest");
    }

    @Test
    void refusesManifestDeclaringUnknownPlatform() {
        // given
        PlatformManifestLoader subject = loaderOf("classpath:axelix/platforms/broken/unknown-platform.yaml");

        // when / then
        assertThatThrownBy(subject::load)
                .isInstanceOf(PlatformCatalogException.class)
                .hasMessageContaining("Failed to read the platform manifest")
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quarkus");
    }

    @Test
    void loadsTheShippedManifestsFromTheDefaultLocation() {
        // given
        PlatformManifestLoader subject = new PlatformManifestLoader();

        // when
        List<Platform> platforms = subject.load();

        // then
        assertThat(platforms)
                .map(Platform::name)
                .contains(
                        PlatformName.SPRING_BOOT,
                        PlatformName.AMAZON_CORRETTO,
                        PlatformName.AZUL_ZULU,
                        PlatformName.BELLSOFT_LIBERICA,
                        PlatformName.ECLIPSE_TEMURIN,
                        PlatformName.IBM_SEMERU,
                        PlatformName.MICROSOFT_OPENJDK,
                        PlatformName.ORACLE_OPENJDK,
                        PlatformName.RED_HAT_OPENJDK,
                        PlatformName.SAP_MACHINE,
                        PlatformName.GRAALVM,
                        PlatformName.ALIBABA_DRAGONWELL,
                        PlatformName.TENCENT_KONA,
                        PlatformName.JETBRAINS);
    }

    @ParameterizedTest
    @CsvSource({
        "sapmachine, 2028-09-30",
        "graalvm-ce, 2024-03-19",
        "alibaba-dragonwell, 2029-11-30",
        "tencent-kona,",
        "jetbrains-runtime,"
    })
    void resolvesJava21ToVendorMaintenanceWindow(String platform, LocalDate deadline) {
        Platform vendor = loaderOf("classpath:axelix/platforms/" + platform + ".yaml")
                .load()
                .getFirst();

        PlatformReleaseLine line = vendor.lineOf("21.0.2+13").orElseThrow();

        assertThat(line.line()).isEqualTo("21");
        assertThat(line.ossSupportEndsAt()).isEqualTo(deadline);
        assertThat(line.ossSupportedAt(LocalDate.of(2024, 3, 1))).isEqualTo(deadline != null);
    }

    @Test
    void resolvesSemeruRuntimeToPublicSupportWindow() {
        Platform semeru =
                loaderOf("classpath:axelix/platforms/ibm-semeru.yaml").load().getFirst();

        assertThat(semeru.lineOf("21.0.1+12"))
                .contains(new PlatformReleaseLine("21", LocalDate.of(2024, 1, 11), LocalDate.of(2031, 9, 30), null));
    }

    @Test
    void resolvesMicrosoftRuntimeToPublicSupportWindow() {
        Platform microsoft = loaderOf("classpath:axelix/platforms/microsoft-openjdk.yaml")
                .load()
                .getFirst();

        assertThat(microsoft.lineOf("11.0.33+10-LTS"))
                .contains(new PlatformReleaseLine("11", LocalDate.of(2021, 5, 25), LocalDate.of(2027, 9, 30), null));
    }

    @Test
    void resolvesRedHatRuntimeToStandardMaintenanceWindow() {
        Platform redHat = loaderOf("classpath:axelix/platforms/red-hat-openjdk.yaml")
                .load()
                .getFirst();

        assertThat(redHat.lineOf("11.0.25+9"))
                .contains(new PlatformReleaseLine("11", LocalDate.of(2018, 10, 1), LocalDate.of(2024, 10, 31), null));
    }

    @Test
    void resolvesZuluRuntimeToPublicSupportWindow() {
        Platform zulu =
                loaderOf("classpath:axelix/platforms/azul-zulu.yaml").load().getFirst();

        assertThat(zulu.lineOf("21.0.4+7-LTS"))
                .contains(new PlatformReleaseLine("21", LocalDate.of(2023, 9, 19), LocalDate.of(2031, 9, 30), null));
    }

    @Test
    void resolvesOracleOpenJdkRuntimeToOssSupportWindow() {
        Platform oracle = loaderOf("classpath:axelix/platforms/oracle-openjdk.yaml")
                .load()
                .getFirst();

        assertThat(oracle.lineOf("21.0.2+13"))
                .contains(new PlatformReleaseLine("21", LocalDate.of(2023, 9, 19), LocalDate.of(2024, 3, 19), null));
    }

    @Test
    void resolvesLibericaRuntimeToPublicSupportWindow() {
        Platform liberica = loaderOf("classpath:axelix/platforms/bellsoft-liberica.yaml")
                .load()
                .getFirst();

        assertThat(liberica.lineOf("27.0.1.1+5"))
                .contains(new PlatformReleaseLine("27", LocalDate.of(2026, 9, 16), LocalDate.of(2027, 3, 23), null));
    }

    @Test
    void resolvesTemurinRuntimeToCommunitySupportWindow() {
        Platform temurin = loaderOf("classpath:axelix/platforms/eclipse-temurin.yaml")
                .load()
                .getFirst();

        assertThat(temurin.lineOf("21.0.13.1+2"))
                .contains(new PlatformReleaseLine("21", LocalDate.of(2023, 10, 10), LocalDate.of(2029, 12, 31), null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"21", "21.0.8", "21.0.8+9-LTS"})
    void resolvesCorrettoVersionsToTheirFeatureRelease(String version) {
        // given.
        Platform corretto = loaderOf("classpath:axelix/platforms/amazon-corretto.yaml")
                .load()
                .getFirst();

        // when / then.
        assertThat(corretto.lineOf(version)).map(PlatformReleaseLine::line).contains("21");
    }

    @ParameterizedTest
    @ValueSource(strings = {"14", "21.invalid", "unknown"})
    void returnsEmptyForUnavailableOrInvalidCorrettoVersions(String version) {
        // given.
        Platform corretto = loaderOf("classpath:axelix/platforms/amazon-corretto.yaml")
                .load()
                .getFirst();

        // when / then.
        assertThat(corretto.lineOf(version)).isEmpty();
    }

    private static PlatformManifestLoader loaderOf(String locationPattern) {
        return new PlatformManifestLoader(new PathMatchingResourcePatternResolver(), locationPattern);
    }
}
