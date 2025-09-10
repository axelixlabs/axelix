package com.nucleonforge.axile.master.service.convert;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.info.ServiceInfo;
import com.nucleonforge.axile.common.api.info.components.BuildInfo;
import com.nucleonforge.axile.common.api.info.components.GitInfo;
import com.nucleonforge.axile.common.api.info.components.JavaInfo;
import com.nucleonforge.axile.common.api.info.components.OSInfo;
import com.nucleonforge.axile.common.api.info.components.ProcessInfo;
import com.nucleonforge.axile.common.api.info.components.SSLInfo;
import com.nucleonforge.axile.master.api.response.info.InfoResponse;
import com.nucleonforge.axile.master.api.response.info.components.BuildProfile;
import com.nucleonforge.axile.master.api.response.info.components.GitProfile;
import com.nucleonforge.axile.master.api.response.info.components.JavaProfile;
import com.nucleonforge.axile.master.api.response.info.components.OSProfile;
import com.nucleonforge.axile.master.api.response.info.components.ProcessProfile;
import com.nucleonforge.axile.master.api.response.info.components.SSLProfile;
import com.nucleonforge.axile.master.service.convert.info.ServiceInfoConverter;
import com.nucleonforge.axile.master.service.convert.info.components.BuildInfoConverter;
import com.nucleonforge.axile.master.service.convert.info.components.GitInfoConverter;
import com.nucleonforge.axile.master.service.convert.info.components.JavaInfoConverter;
import com.nucleonforge.axile.master.service.convert.info.components.OSInfoConverter;
import com.nucleonforge.axile.master.service.convert.info.components.ProcessInfoConverter;
import com.nucleonforge.axile.master.service.convert.info.components.SSLInfoConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ServiceInfoConverter}
 *
 * @author Sergey Cherkasov
 */
public class ServiceInfoConverterTest {
    private ServiceInfoConverter subject;

    @BeforeEach
    void setUp() {
        subject = new ServiceInfoConverter(
                new BuildInfoConverter(),
                new GitInfoConverter(),
                new JavaInfoConverter(),
                new OSInfoConverter(),
                new SSLInfoConverter(),
                new ProcessInfoConverter());
    }

    @Test
    void testConvertHappyPath() {
        // when.
        InfoResponse response = subject.convertInternal(getInfo());

        // then.
        testComponentBuildConvertHappyPath(response);
        testComponentGitConvertHappyPath(response);
        testComponentOSConvertHappyPath(response);
        testComponentProcessConvertHappyPath(response);
        testComponentProcessMemoryHeapAndNonHeapConvertHappyPath(response);
        testComponentJavaHappyPath(response);
        testComponentSSLConvertHappyPath(response);
    }

    private void testComponentBuildConvertHappyPath(InfoResponse response) {
        BuildProfile buildProfile = response.build();
        assertThat(buildProfile.artifact()).isEqualTo("application");
        assertThat(buildProfile.group()).isEqualTo("com.example");
        assertThat(buildProfile.name()).isEqualTo("axile");
        assertThat(buildProfile.version()).isEqualTo("1.0.3");
        assertThat(buildProfile.time()).isNull();
    }

    private void testComponentGitConvertHappyPath(InfoResponse response) {
        GitProfile gitProfile = response.git();
        assertThat(gitProfile.branch()).isEqualTo("main");
        assertThat(gitProfile.commit().id()).isEqualTo("df027cf");
        assertThat(gitProfile.commit().time()).isEqualTo("2025-08-21T09:11:35Z");
    }

    private void testComponentOSConvertHappyPath(InfoResponse response) {
        OSProfile osProfile = response.os();
        assertThat(osProfile.name()).isEqualTo("Linux");
        assertThat(osProfile.version()).isEqualTo("6.11.0-1018-azure");
        assertThat(osProfile.arch()).isEqualTo("amd64");
    }

    private void testComponentProcessConvertHappyPath(InfoResponse response) {
        ProcessProfile processProfile = response.process();
        assertThat(processProfile.pid()).isEqualTo(91003L);
        assertThat(processProfile.parentPid()).isEqualTo(89732L);
        assertThat(processProfile.owner()).isEqualTo("runner");
        assertThat(processProfile.cpus()).isEqualTo(4);
    }

    private void testComponentProcessMemoryHeapAndNonHeapConvertHappyPath(InfoResponse response) {
        ProcessProfile.Memory memory =
                response.process() != null ? response.process().memory() : null;

        // ProcessProfile.Memory.Heap
        ProcessProfile.Memory.Heap heap = memory != null ? memory.heap() : null;
        if (heap != null) {
            assertThat(heap.init()).isEqualTo(262144000L);
            assertThat(heap.used()).isEqualTo(97295984L);
            assertThat(heap.committed()).isEqualTo(143654912L);
            assertThat(heap.max()).isEqualTo(1610612736L);
        }

        // ProcessProfile.Memory.NonHeap
        ProcessProfile.Memory.NonHeap nonHeap = memory != null ? memory.nonHeap() : null;
        if (nonHeap != null) {
            assertThat(nonHeap.init()).isEqualTo(7667712L);
            assertThat(nonHeap.used()).isEqualTo(92845808L);
            assertThat(nonHeap.committed()).isEqualTo(95485952L);
            assertThat(nonHeap.max()).isEqualTo(-1L);
        }

        // ProcessProfile.Memory.GarbageCollectors
        if (memory != null) {
            Set<ProcessProfile.Memory.GarbageCollectors> garbageCollectors = memory.garbageCollectors() == null
                            || memory.garbageCollectors().isEmpty()
                    ? Collections.emptySet()
                    : memory.garbageCollectors();

            // G1 Young Generation
            assertThat(garbageCollectors)
                    .hasSize(2)
                    .filteredOn(g -> g.name().equals("G1 Young Generation"))
                    .first()
                    .satisfies(g -> assertThat(g.collectionCount()).isEqualTo(15));

            // G1 Old Generation
            assertThat(garbageCollectors)
                    .filteredOn(g -> g.name().equals("G1 Old Generation"))
                    .first()
                    .satisfies(g -> assertThat(g.collectionCount()).isEqualTo(0));
        }

        // ProcessProfile.VirtualThreads
        ProcessProfile.VirtualThreads virtualThreads = response.process().virtualThreads();
        assertThat(virtualThreads).isNull();
    }

    private void testComponentJavaHappyPath(InfoResponse response) {
        // JavaProfile
        JavaProfile javaProfile = response.java();
        assertThat(javaProfile.version()).isEqualTo("17.0.16");

        // JavaProfile.Vendor
        JavaProfile.Vendor vendor = javaProfile.vendor();
        assertThat(vendor.name()).isEqualTo("BellSoft");
        assertThat(vendor.version()).isNull();

        // JavaProfile.JVM
        JavaProfile.JVM jvm = javaProfile.jvm();
        assertThat(jvm.name()).isEqualTo("OpenJDK 64-Bit Server VM");
        assertThat(jvm.vendor()).isEqualTo("BellSoft");
        assertThat(jvm.version()).isEqualTo("17.0.16+12-LTS");

        // JavaProfile.Runtime
        JavaProfile.Runtime runtime = javaProfile.runtime();
        assertThat(runtime.name()).isEqualTo("OpenJDK Runtime Environment");
        assertThat(runtime.version()).isEqualTo("17.0.16+12-LTS");
    }

    private void testComponentSSLConvertHappyPath(InfoResponse response) {
        // SSLProfile.Bundles
        Set<SSLProfile.Bundles> bundlesArray = response.ssl().bundles();
        assertThat(bundlesArray).hasSize(1);

        SSLProfile.Bundles bundles = bundlesArray.iterator().next();
        assertThat(bundles.name()).isEqualTo("test-0");

        // SSLProfile.Bundles.CertificateChains -> "spring-boot", "test-alias"
        String springTest = "spring-boot";
        String testAlias = "test-alias";

        Set<SSLProfile.Bundles.CertificateChains> certificateChainsArrays = bundles.certificateChains();
        assertThat(certificateChainsArrays)
                .hasSize(2)
                .extracting(SSLProfile.Bundles.CertificateChains::alias)
                .containsOnly(springTest, testAlias);

        // SSLProfile.Bundles.CertificateChains -> "spring-boot"
        SSLProfile.Bundles.CertificateChains certificateChainsSpringBoot = certificateChainsArrays.stream()
                .filter(c -> c.alias().equals(springTest))
                .findFirst()
                .orElseThrow();

        assertThat(certificateChainsSpringBoot.certificates()).hasSize(1);

        // SSLProfile.Bundles.CertificateChains -> "spring-boot" ->
        // -> SSLProfile.Bundles.CertificateChains.Certificates
        SSLProfile.Bundles.CertificateChains.Certificates certificateSpringBoot =
                certificateChainsSpringBoot.certificates().iterator().next();

        assertThat(certificateSpringBoot.subject())
                .isEqualTo("CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US");
        assertThat(certificateSpringBoot.version()).isEqualTo("V3");
        assertThat(certificateSpringBoot.issuer())
                .isEqualTo("CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US");
        assertThat(certificateSpringBoot.validityStarts()).isEqualTo("2023-05-05T11:26:57Z");
        assertThat(certificateSpringBoot.serialNumber()).isEqualTo("eb6114a6ae39ce6c");
        assertThat(certificateSpringBoot.validityEnds()).isEqualTo("2123-04-11T11:26:57Z");
        assertThat(certificateSpringBoot.signatureAlgorithmName()).isEqualTo("SHA256withRSA");

        // SSLProfile.Bundles.CertificateChains -> "spring-boot" ->
        // ->  SSLProfile.Bundles.CertificateChains.Certificates.Validity
        assertThat(certificateSpringBoot.validity().status()).isEqualTo("VALID");

        // SSLProfile.Bundles.CertificateChains -> "test-alias"
        SSLProfile.Bundles.CertificateChains certificateChainsTestAlias = certificateChainsArrays.stream()
                .filter(c -> c.alias().equals(testAlias))
                .findFirst()
                .orElseThrow();

        assertThat(certificateChainsSpringBoot.certificates()).hasSize(1);

        // SSLProfile.Bundles.CertificateChains -> "test-alias" ->
        // -> SSLProfile.Bundles.CertificateChains.Certificates
        SSLProfile.Bundles.CertificateChains.Certificates certificateTestAlias =
                certificateChainsTestAlias.certificates().iterator().next();

        assertThat(certificateTestAlias.subject())
                .isEqualTo("CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US");
        assertThat(certificateTestAlias.version()).isEqualTo("V3");
        assertThat(certificateTestAlias.issuer())
                .isEqualTo("CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US");
        assertThat(certificateTestAlias.validityStarts()).isEqualTo("2023-05-05T11:26:58Z");
        assertThat(certificateTestAlias.serialNumber()).isEqualTo("14ca9ba6abe2a70d");
        assertThat(certificateTestAlias.validityEnds()).isEqualTo("2123-04-11T11:26:58Z");
        assertThat(certificateTestAlias.signatureAlgorithmName()).isEqualTo("SHA256withRSA");

        // SSLProfile.Bundles.CertificateChains -> "test-alias" ->
        // -> SSLProfile.Bundles.CertificateChains.Certificates.Validity
        assertThat(certificateSpringBoot.validity().status()).isEqualTo("VALID");
    }

    private static ServiceInfo getInfo() {
        // BuildInfo
        BuildInfo build = new BuildInfo("application", "axile", "1.0.3", "com.example", null);

        // GitInfo
        GitInfo git = new GitInfo("main", new GitInfo.Commit("df027cf", "2025-08-21T09:11:35Z"));

        // OSSInfo
        OSInfo os = new OSInfo("Linux", "6.11.0-1018-azure", "amd64");

        // ProcessInfo
        ProcessInfo.Memory.Heap heap = new ProcessInfo.Memory.Heap(1610612736L, 97295984L, 143654912L, 262144000L);
        ProcessInfo.Memory.NonHeap nonHeap = new ProcessInfo.Memory.NonHeap(-1L, 92845808L, 95485952L, 7667712L);
        ProcessInfo.Memory.GarbageCollectors garbageCollectors1 =
                new ProcessInfo.Memory.GarbageCollectors("G1 Young Generation", 15);
        ProcessInfo.Memory.GarbageCollectors garbageCollectors2 =
                new ProcessInfo.Memory.GarbageCollectors("G1 Old Generation", 0);
        ProcessInfo.Memory memory =
                new ProcessInfo.Memory(heap, nonHeap, Set.of(garbageCollectors1, garbageCollectors2));
        ProcessInfo process = new ProcessInfo(91003L, 89732L, "runner", memory, null, 4);

        // JavaInfo
        JavaInfo.Vendor vendor = new JavaInfo.Vendor("BellSoft", null);
        JavaInfo.Runtime runtime = new JavaInfo.Runtime("OpenJDK Runtime Environment", "17.0.16+12-LTS");
        JavaInfo.JVM jvm = new JavaInfo.JVM("OpenJDK 64-Bit Server VM", "BellSoft", "17.0.16+12-LTS");
        JavaInfo java = new JavaInfo("17.0.16", vendor, runtime, jvm);

        // SSLInfo
        SSLInfo.Bundles.CertificateChains.Certificates.Validity validity =
                new SSLInfo.Bundles.CertificateChains.Certificates.Validity("VALID");
        SSLInfo.Bundles.CertificateChains.Certificates certificatesSpringBoot =
                new SSLInfo.Bundles.CertificateChains.Certificates(
                        "V3",
                        "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                        validity,
                        "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                        "eb6114a6ae39ce6c",
                        "SHA256withRSA",
                        "2023-05-05T11:26:57Z",
                        "2123-04-11T11:26:57Z");
        SSLInfo.Bundles.CertificateChains.Certificates certificatesTestAlias =
                new SSLInfo.Bundles.CertificateChains.Certificates(
                        "V3",
                        "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                        validity,
                        "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                        "14ca9ba6abe2a70d",
                        "SHA256withRSA",
                        "2023-05-05T11:26:58Z",
                        "2123-04-11T11:26:58Z");
        SSLInfo.Bundles.CertificateChains certificateChainsSpringBoot =
                new SSLInfo.Bundles.CertificateChains("spring-boot", Set.of(certificatesSpringBoot));
        SSLInfo.Bundles.CertificateChains certificateChainsTestAlias =
                new SSLInfo.Bundles.CertificateChains("test-alias", Set.of(certificatesTestAlias));
        SSLInfo.Bundles bundles =
                new SSLInfo.Bundles("test-0", Set.of(certificateChainsSpringBoot, certificateChainsTestAlias));
        SSLInfo ssl = new SSLInfo(Set.of(bundles));

        // ServiceInfo -> returned
        return new ServiceInfo(git, build, os, process, java, ssl);
    }
}
