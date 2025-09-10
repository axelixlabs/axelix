package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.info.ServiceInfo;
import com.nucleonforge.axile.common.api.info.components.BuildInfo;
import com.nucleonforge.axile.common.api.info.components.GitInfo;
import com.nucleonforge.axile.common.api.info.components.JavaInfo;
import com.nucleonforge.axile.common.api.info.components.OSInfo;
import com.nucleonforge.axile.common.api.info.components.ProcessInfo;
import com.nucleonforge.axile.common.api.info.components.SSLInfo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link InfoJacksonMessageDeserializationStrategy}.
 *
 * @author Sergey Cherkasov
 */
public class InfoJacksonMessageDeserializationStrategyTest {

    private final InfoJacksonMessageDeserializationStrategy subject =
            new InfoJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeServiceInfo() {
        // when.
        // language=json
        String response =
                """
        {
          "git" : {
            "branch" : "main",
            "commit" : {
              "id" : "df027cf",
              "time" : "2025-08-21T09:11:35Z"
            }
          },
          "build" : {
            "artifact" : "application",
            "version" : "1.0.3",
            "group" : "com.example"
          },
          "os" : {
            "name" : "Linux",
            "version" : "6.11.0-1018-azure",
            "arch" : "amd64"
          },
          "process" : {
            "pid" : 91003,
            "parentPid" : 89732,
            "owner" : "runner",
            "memory" : {
              "heap" : {
                "max" : 1610612736,
                "used" : 97295984,
                "committed" : 143654912,
                "init" : 262144000
              },
              "nonHeap" : {
                "max" : -1,
                "used" : 92845808,
                "committed" : 95485952,
                "init" : 7667712
              },
              "garbageCollectors" : [ {
                "name" : "G1 Young Generation",
                "collectionCount" : 15
              }, {
                "name" : "G1 Old Generation",
                "collectionCount" : 0
              } ]
            },
            "cpus" : 4
          },
          "java" : {
            "version" : "17.0.16",
            "vendor" : {
              "name" : "BellSoft"
            },
            "runtime" : {
              "name" : "OpenJDK Runtime Environment",
              "version" : "17.0.16+12-LTS"
            },
            "jvm" : {
              "name" : "OpenJDK 64-Bit Server VM",
              "vendor" : "BellSoft",
              "version" : "17.0.16+12-LTS"
            }
          },
          "ssl" : {
            "bundles" : [ {
              "name" : "test-0",
              "certificateChains" : [ {
                "alias" : "spring-boot",
                "certificates" : [ {
                  "version" : "V3",
                  "issuer" : "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                  "validity" : {
                    "status" : "VALID"
                  },
                  "subject" : "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                  "serialNumber" : "eb6114a6ae39ce6c",
                  "signatureAlgorithmName" : "SHA256withRSA",
                  "validityStarts" : "2023-05-05T11:26:57Z",
                  "validityEnds" : "2123-04-11T11:26:57Z"
                } ]
              }, {
                "alias" : "test-alias",
                "certificates" : [ {
                  "version" : "V3",
                  "issuer" : "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                  "validity" : {
                    "status" : "VALID"
                  },
                  "subject" : "CN=localhost,OU=Spring,O=VMware,L=Palo Alto,ST=California,C=US",
                  "serialNumber" : "14ca9ba6abe2a70d",
                  "signatureAlgorithmName" : "SHA256withRSA",
                  "validityStarts" : "2023-05-05T11:26:58Z",
                  "validityEnds" : "2123-04-11T11:26:58Z"
                } ]
              }, {
                "alias" : "spring-boot-cert",
                "certificates" : [ ]
              }, {
                "alias" : "test-alias-cert",
                "certificates" : [ ]
              } ]
            } ]
          }
        }
        """;

        ServiceInfo serviceInfo = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        // then.
        shouldDeserializeGitInfo(serviceInfo);
        shouldDeserializeBuildInfo(serviceInfo);
        shouldDeserializeOSInfo(serviceInfo);
        shouldDeserializeProcessInfo(serviceInfo);
        shouldDeserializeJavaInfo(serviceInfo);
        shouldDeserializeSSLInfo(serviceInfo);
    }

    private void shouldDeserializeGitInfo(ServiceInfo serviceInfo) {
        GitInfo git = serviceInfo.git();
        assertThat(git.branch()).isEqualTo("main");
        assertThat(git.commit().id()).isEqualTo("df027cf");
        assertThat(git.commit().time()).isEqualTo("2025-08-21T09:11:35Z");
    }

    private void shouldDeserializeBuildInfo(ServiceInfo serviceInfo) {
        BuildInfo build = serviceInfo.build();
        assertThat(build.artifact()).isEqualTo("application");
        assertThat(build.name()).isNull();
        assertThat(build.version()).isEqualTo("1.0.3");
        assertThat(build.group()).isEqualTo("com.example");
        assertThat(build.time()).isNull();
    }

    private void shouldDeserializeOSInfo(ServiceInfo serviceInfo) {
        OSInfo os = serviceInfo.os();
        assertThat(os.arch()).isEqualTo("amd64");
        assertThat(os.name()).isEqualTo("Linux");
        assertThat(os.version()).isEqualTo("6.11.0-1018-azure");
    }

    private void shouldDeserializeProcessInfo(ServiceInfo serviceInfo) {
        // Process.Memory.Heap;
        ProcessInfo.Memory.Heap heap = serviceInfo.process().memory().heap();
        assertThat(heap.max()).isEqualTo(1610612736);
        assertThat(heap.committed()).isEqualTo(143654912);
        assertThat(heap.init()).isEqualTo(262144000);
        assertThat(heap.used()).isEqualTo(97295984);

        // Process.Memory.NonHeap
        ProcessInfo.Memory.NonHeap nonHeap = serviceInfo.process().memory().nonHeap();
        assertThat(nonHeap.max()).isEqualTo(-1);
        assertThat(nonHeap.committed()).isEqualTo(95485952);
        assertThat(nonHeap.init()).isEqualTo(7667712);
        assertThat(nonHeap.used()).isEqualTo(92845808);

        // Process.Memory.GarbageCollectors
        Set<ProcessInfo.Memory.GarbageCollectors> garbageCollectors =
                serviceInfo.process().memory().garbageCollectors();
        assertThat(garbageCollectors)
                .hasSize(2)
                .filteredOn(g -> g.name().equals("G1 Young Generation"))
                .first()
                .satisfies(g -> assertThat(g.collectionCount()).isEqualTo(15));
        assertThat(garbageCollectors)
                .filteredOn(g -> g.name().equals("G1 Old Generation"))
                .first()
                .satisfies(g -> assertThat(g.collectionCount()).isEqualTo(0));

        // Process.VirtualThreads
        assertThat(serviceInfo.process().virtualThreads()).isNull();
    }

    private void shouldDeserializeJavaInfo(ServiceInfo serviceInfo) {
        // Java
        assertThat(serviceInfo.java().version()).isEqualTo("17.0.16");

        // Java.JVM
        JavaInfo.JVM jvm = serviceInfo.java().jvm();
        assertThat(jvm.name()).isEqualTo("OpenJDK 64-Bit Server VM");
        assertThat(jvm.version()).isEqualTo("17.0.16+12-LTS");
        assertThat(jvm.vendor()).isEqualTo("BellSoft");

        // Java.Runtime
        JavaInfo.Runtime runtime = serviceInfo.java().runtime();
        assertThat(runtime.name()).isEqualTo("OpenJDK Runtime Environment");
        assertThat(runtime.version()).isEqualTo("17.0.16+12-LTS");

        // Java.Vendor
        JavaInfo.Vendor vendor = serviceInfo.java().vendor();
        assertThat(vendor.name()).isEqualTo("BellSoft");
        assertThat(vendor.version()).isNull();
    }

    private void shouldDeserializeSSLInfo(ServiceInfo serviceInfo) {
        // SSL.Bundles
        Set<SSLInfo.Bundles> bundlesArray = serviceInfo.ssl().bundles();
        assertThat(bundlesArray).hasSize(1);
        SSLInfo.Bundles bundles = bundlesArray.iterator().next();
        assertThat(bundles.name()).isEqualTo("test-0");

        // SSL.Bundles.CertificateChains -> "spring-boot", "test-alias"
        String springTest = "spring-boot";
        String testAlias = "test-alias";

        Set<SSLInfo.Bundles.CertificateChains> certificateChainsArrays = bundles.certificateChains();
        assertThat(certificateChainsArrays)
                .hasSize(4)
                .extracting(SSLInfo.Bundles.CertificateChains::alias)
                .containsOnly("spring-boot-cert", "test-alias-cert", springTest, testAlias);

        // SSL.Bundles.CertificateChains -> "spring-boot"
        SSLInfo.Bundles.CertificateChains certificateChainsSpringBoot = certificateChainsArrays.stream()
                .filter(c -> c.alias().equals(springTest))
                .findFirst()
                .orElseThrow();

        assertThat(certificateChainsSpringBoot.certificates()).hasSize(1);

        // SSL.Bundles.CertificateChains -> "spring-boot" ->
        // -> SSL.Bundles.CertificateChains.Certificates
        SSLInfo.Bundles.CertificateChains.Certificates certificateSpringBoot =
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

        // SSL.Bundles.CertificateChains -> "spring-boot" ->
        // ->  SSL.Bundles.CertificateChains.Certificates.Validity
        assertThat(certificateSpringBoot.validity().status()).isEqualTo("VALID");

        // SSL.Bundles.CertificateChains -> "test-alias"
        SSLInfo.Bundles.CertificateChains certificateChainsTestAlias = certificateChainsArrays.stream()
                .filter(c -> c.alias().equals(testAlias))
                .findFirst()
                .orElseThrow();

        assertThat(certificateChainsSpringBoot.certificates()).hasSize(1);

        // SSL.Bundles.CertificateChains -> "test-alias" ->
        // -> SSL.Bundles.CertificateChains.Certificates
        SSLInfo.Bundles.CertificateChains.Certificates certificateTestAlias =
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

        // SSL.Bundles.CertificateChains -> "test-alias" ->
        // -> SSL.Bundles.CertificateChains.Certificates.Validity
        assertThat(certificateSpringBoot.validity().status()).isEqualTo("VALID");
    }
}
