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
package com.axelixlabs.axelix.master.api.external.endpoint.instance;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.contract.quartz.QuartzCronExpressionValidationRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobExecuteRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobPauseRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobResumeRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobsRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTriggerPauseRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTriggerResumeRequest;
import com.axelixlabs.axelix.master.api.external.request.QuartzSimpleTriggerIntervalValidationRequest;
import com.axelixlabs.axelix.master.api.external.response.QuartzSimpleTriggerIntervalValidationResponse;
import com.axelixlabs.axelix.master.domain.auth.Authority;
import com.axelixlabs.axelix.master.domain.auth.DefaultRole;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoint;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.state.auth.RoleService;
import com.axelixlabs.axelix.master.utils.IdentityAwareTestRestTemplate;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link QuartzApi}.
 *
 * @author Nikita Kirillov
 */
class QuartzApiTest extends AbstractProtectedEndpointTest {

    private static final String SCHEDULERS_JSON = """
            {"schedulers":[{
              "schedulerName":"quartzScheduler",
              "schedulerInstanceId":"NON_CLUSTERED",
              "version":"2.5.2",
              "started":true,
              "jobStoreClustered":false,
              "threadPoolSize":10
            }]}
            """;

    private static final String JOBS_JSON = """
            {"jobs":[{
              "name":"sample", "group":"jobs", "className":"example.SampleJob",
              "durable":true, "concurrentExecutionDisallowed":true,
              "triggers":[{
                "name":"cron", "group":"triggers", "type":"CRON", "state":"NORMAL",
                "cronExpression":"0 0 12 * * ?", "misfirePolicy":"DO_NOTHING"
              }]
            }],"executions":[{
              "executionId":"execution-1", "jobName":"sample", "jobGroup":"jobs",
              "triggerName":"cron", "triggerGroup":"triggers", "startedAt":"2026-11-26T08:00:00Z"
            }]}
            """;

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private RoleService roleService;

    private final String instanceId = UUID.randomUUID().toString();

    private MockWebServer server;

    @BeforeEach
    void prepare() throws IOException {
        server = new MockWebServer();
        server.start();
        registry.register(
                TestInstanceFactory.create(instanceId, server.url("/actuator").toString()));
    }

    @AfterEach
    void cleanup() throws IOException {
        registry.deRegister(InstanceId.of(instanceId));
        server.shutdown();
    }

    @Nested
    class GetQuartzSchedulers {

        @ParameterizedTest
        @ValueSource(strings = {SCHEDULERS_JSON, "{\"schedulers\":[]}"})
        void shouldReturnSchedulers(String body) throws InterruptedException {
            // given.
            server.enqueue(new MockResponse().setBody(body).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));
            IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();

            // when.
            ResponseEntity<String> response =
                    viewer.getForEntity("/api/external/quartz/{instanceId}", String.class, instanceId);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
            assertThatJson(response.getBody()).isEqualTo(body);
            RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getMethod()).isEqualTo("GET");
            assertThat(request.getPath()).isEqualTo("/actuator/axelix-quartz");
            assertThat(request.getHeader("Authorization")).startsWith("Bearer ");
            assertSuccessfulCallback(MasterWebEndpoints.QUARTZ_SCHEDULERS_READ, viewer.getActor());
        }

        @ParameterizedTest
        @ValueSource(ints = {400, 404, 500})
        void shouldHandleStarterError(int status) {
            // given.
            server.enqueue(new MockResponse().setResponseCode(status));

            // when.
            ResponseEntity<String> response =
                    restTemplate.asViewer().getForEntity("/api/external/quartz/{instanceId}", String.class, instanceId);

            // then.
            assertThat(response.getStatusCode())
                    .isEqualTo(status == 400 ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    class GetQuartzJobs {

        @ParameterizedTest
        @MethodSource("com.axelixlabs.axelix.master.api.external.endpoint.instance.QuartzApiTest#jobsRequests")
        void shouldReturnJobs(String body, String schedulerName) throws InterruptedException {
            // given.
            server.enqueue(new MockResponse().setBody(body).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));
            IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();

            // when.
            ResponseEntity<String> response = viewer.postForEntity(
                    "/api/external/quartz/{instanceId}/jobs",
                    new QuartzJobsRequest().schedulerName(schedulerName),
                    String.class,
                    instanceId);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
            assertThatJson(response.getBody()).isEqualTo(body);
            RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/actuator/axelix-quartz/jobs");
            assertThatJson(request.getBody().readUtf8()).node("schedulerName").isEqualTo(schedulerName);
            assertSuccessfulCallback(MasterWebEndpoints.QUARTZ_JOBS_READ, viewer.getActor());
        }

        @ParameterizedTest
        @ValueSource(ints = {400, 404, 500})
        void shouldHandleStarterError(int status) {
            // given.
            server.enqueue(new MockResponse().setResponseCode(status));

            // when.
            ResponseEntity<String> response = restTemplate
                    .asViewer()
                    .postForEntity(
                            "/api/external/quartz/{instanceId}/jobs",
                            new QuartzJobsRequest().schedulerName("missing"),
                            String.class,
                            instanceId);

            // then.
            assertThat(response.getStatusCode())
                    .isEqualTo(status == 400 ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    class ValidateCronExpression {

        @Test
        void shouldProxyValidationToStarter() throws InterruptedException {
            // given.
            String responseBody = "{\"valid\":true}";
            server.enqueue(
                    new MockResponse().setBody(responseBody).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));
            QuartzCronExpressionValidationRequest payload =
                    new QuartzCronExpressionValidationRequest().cronExpression("0 0 12 * * ? 2027");
            IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();

            // when.
            ResponseEntity<String> response = viewer.postForEntity(
                    "/api/external/quartz/{instanceId}/validateCronExpression", payload, String.class, instanceId);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
            assertThatJson(response.getBody()).isEqualTo(responseBody);
            RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/actuator/axelix-quartz/validateCronExpression");
            assertThatJson(request.getBody().readUtf8()).isEqualTo("""
                    {"cronExpression":"0 0 12 * * ? 2027"}
                    """);
            assertSuccessfulCallback(MasterWebEndpoints.QUARTZ_CRON_EXPRESSION_VALIDATE, viewer.getActor());
        }
    }

    @Nested
    class ValidateSimpleTriggerInterval {

        @ParameterizedTest
        @ValueSource(longs = {1, 30_000})
        void shouldAcceptPositiveInterval(long interval) {
            // given.
            QuartzSimpleTriggerIntervalValidationRequest payload =
                    new QuartzSimpleTriggerIntervalValidationRequest(interval);
            IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();

            // when.
            ResponseEntity<QuartzSimpleTriggerIntervalValidationResponse> response = viewer.postForEntity(
                    "/api/external/quartz/validateSimpleTriggerInterval",
                    payload,
                    QuartzSimpleTriggerIntervalValidationResponse.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().valid()).isTrue();
            assertThat(server.getRequestCount()).isZero();
            assertSuccessfulCallback(MasterWebEndpoints.QUARTZ_SIMPLE_TRIGGER_INTERVAL_VALIDATE, viewer.getActor());
        }

        @ParameterizedTest
        @ValueSource(longs = {0, -1})
        void shouldRejectNonPositiveInterval(long interval) {
            // given.
            QuartzSimpleTriggerIntervalValidationRequest payload =
                    new QuartzSimpleTriggerIntervalValidationRequest(interval);

            // when.
            ResponseEntity<QuartzSimpleTriggerIntervalValidationResponse> response = restTemplate
                    .asViewer()
                    .postForEntity(
                            "/api/external/quartz/validateSimpleTriggerInterval",
                            payload,
                            QuartzSimpleTriggerIntervalValidationResponse.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().valid()).isFalse();
            assertThat(server.getRequestCount()).isZero();
        }

        @Test
        void shouldRejectIncompleteRequest() {
            // given.
            QuartzSimpleTriggerIntervalValidationRequest payload =
                    new QuartzSimpleTriggerIntervalValidationRequest(null);

            // when.
            ResponseEntity<Void> response = restTemplate
                    .asViewer()
                    .postForEntity("/api/external/quartz/validateSimpleTriggerInterval", payload, Void.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(server.getRequestCount()).isZero();
        }
    }

    @Nested
    class ExecuteQuartzJob {

        @Test
        void shouldExecuteJob() throws InterruptedException {
            // given.
            server.enqueue(new MockResponse().setResponseCode(204));
            QuartzJobExecuteRequest payload = new QuartzJobExecuteRequest()
                    .schedulerName("quartzScheduler")
                    .jobName("sample name")
                    .jobGroup("sample group");
            IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

            // when.
            ResponseEntity<Void> response =
                    editor.postForEntity("/api/external/quartz/{instanceId}/execute", payload, Void.class, instanceId);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/actuator/axelix-quartz/execute");
            assertThat(request.getHeader("Content-Type")).startsWith(MediaType.APPLICATION_JSON_VALUE);
            assertThatJson(request.getBody().readUtf8()).isEqualTo("""
                    {"schedulerName":"quartzScheduler","jobName":"sample name","jobGroup":"sample group"}
                    """);
            assertSuccessfulCallback(MasterWebEndpoints.QUARTZ_JOB_EXECUTE, editor.getActor());
        }
    }

    @Nested
    class PauseQuartzJob {

        @Test
        void shouldPauseJob() throws InterruptedException {
            // given.
            server.enqueue(new MockResponse().setResponseCode(204));
            QuartzJobPauseRequest payload = new QuartzJobPauseRequest()
                    .schedulerName("quartzScheduler")
                    .jobName("sample name")
                    .jobGroup("sample group");
            IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

            // when.
            ResponseEntity<Void> response =
                    editor.postForEntity("/api/external/quartz/{instanceId}/pauseJob", payload, Void.class, instanceId);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/actuator/axelix-quartz/pauseJob");
            assertThat(request.getHeader("Content-Type")).startsWith(MediaType.APPLICATION_JSON_VALUE);
            assertThatJson(request.getBody().readUtf8()).isEqualTo("""
                    {"schedulerName":"quartzScheduler","jobName":"sample name","jobGroup":"sample group"}
                    """);
            assertSuccessfulCallback(MasterWebEndpoints.QUARTZ_JOB_PAUSE, editor.getActor());
        }
    }

    @Nested
    class ResumeQuartzJob {

        @Test
        void shouldResumeJob() throws InterruptedException {
            // given.
            server.enqueue(new MockResponse().setResponseCode(204));
            QuartzJobResumeRequest payload = new QuartzJobResumeRequest()
                    .schedulerName("quartzScheduler")
                    .jobName("sample name")
                    .jobGroup("sample group");
            IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

            // when.
            ResponseEntity<Void> response = editor.postForEntity(
                    "/api/external/quartz/{instanceId}/resumeJob", payload, Void.class, instanceId);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/actuator/axelix-quartz/resumeJob");
            assertThat(request.getHeader("Content-Type")).startsWith(MediaType.APPLICATION_JSON_VALUE);
            assertThatJson(request.getBody().readUtf8()).isEqualTo("""
                    {"schedulerName":"quartzScheduler","jobName":"sample name","jobGroup":"sample group"}
                    """);
            assertSuccessfulCallback(MasterWebEndpoints.QUARTZ_JOB_RESUME, editor.getActor());
        }
    }

    @Nested
    class PauseQuartzTrigger {

        @Test
        void shouldPauseTrigger() throws InterruptedException {
            // given.
            server.enqueue(new MockResponse().setResponseCode(204));
            QuartzTriggerPauseRequest payload = new QuartzTriggerPauseRequest()
                    .schedulerName("quartzScheduler")
                    .triggerName("sample name")
                    .triggerGroup("sample group");
            IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

            // when.
            ResponseEntity<Void> response = editor.postForEntity(
                    "/api/external/quartz/{instanceId}/pauseTrigger", payload, Void.class, instanceId);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/actuator/axelix-quartz/pauseTrigger");
            assertThat(request.getHeader("Content-Type")).startsWith(MediaType.APPLICATION_JSON_VALUE);
            assertThatJson(request.getBody().readUtf8()).isEqualTo("""
                    {"schedulerName":"quartzScheduler","triggerName":"sample name","triggerGroup":"sample group"}
                    """);
            assertSuccessfulCallback(MasterWebEndpoints.QUARTZ_TRIGGER_PAUSE, editor.getActor());
        }
    }

    @Nested
    class ResumeQuartzTrigger {

        @Test
        void shouldResumeTrigger() throws InterruptedException {
            // given.
            server.enqueue(new MockResponse().setResponseCode(204));
            QuartzTriggerResumeRequest payload = new QuartzTriggerResumeRequest()
                    .schedulerName("quartzScheduler")
                    .triggerName("sample name")
                    .triggerGroup("sample group");
            IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

            // when.
            ResponseEntity<Void> response = editor.postForEntity(
                    "/api/external/quartz/{instanceId}/resumeTrigger", payload, Void.class, instanceId);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/actuator/axelix-quartz/resumeTrigger");
            assertThat(request.getHeader("Content-Type")).startsWith(MediaType.APPLICATION_JSON_VALUE);
            assertThatJson(request.getBody().readUtf8()).isEqualTo("""
                    {"schedulerName":"quartzScheduler","triggerName":"sample name","triggerGroup":"sample group"}
                    """);
            assertSuccessfulCallback(MasterWebEndpoints.QUARTZ_TRIGGER_RESUME, editor.getActor());
        }
    }

    @ParameterizedTest
    @MethodSource("endpointRequests")
    void shouldRejectUnknownInstance(String path, HttpMethod method, Object payload) {
        // given.
        String unknownInstanceId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .exchange("/api/external" + path, method, new HttpEntity<>(payload), String.class, unknownInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(server.getRequestCount()).isZero();
    }

    @ParameterizedTest
    @MethodSource("endpointRequests")
    void shouldHandleUnavailableInstance(String path, HttpMethod method, Object payload) throws IOException {
        // given.
        server.shutdown();

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .exchange("/api/external" + path, method, new HttpEntity<>(payload), String.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ParameterizedTest
    @MethodSource("managementErrors")
    void shouldHandleManagementError(String path, Object payload, int status) {
        // given.
        server.enqueue(new MockResponse().setResponseCode(status));

        // when.
        ResponseEntity<String> response =
                restTemplate.asEditor().postForEntity("/api/external" + path, payload, String.class, instanceId);

        // then.
        assertThat(response.getStatusCode())
                .isEqualTo(status == 400 ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ParameterizedTest
    @MethodSource("quartzAuthorities")
    void shouldAllowQuartzAuthority(String path, Object payload, MasterWebEndpoint target, String roleName) {
        // given.
        Authority authority = roleService.findByName(roleName).orElseThrow().getEffectiveAuthorities().stream()
                .filter(value -> value.getName().equals("quartz:write"))
                .findFirst()
                .orElseThrow();
        IdentityAwareTestRestTemplate editor = restTemplate.withRole(new DefaultRole("QUARTZ_ONLY", Set.of(authority)));
        server.enqueue(new MockResponse().setResponseCode(204));

        // when.
        ResponseEntity<Void> response = editor.postForEntity("/api/external" + path, payload, Void.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertSuccessfulCallback(target, editor.getActor());
    }

    @ParameterizedTest
    @MethodSource("managementRequests")
    void shouldRejectScheduledTasksAuthority(String path, Object payload, MasterWebEndpoint target) {
        // given.
        Authority authority = roleService.findByName("EDITOR").orElseThrow().getEffectiveAuthorities().stream()
                .filter(value -> value.getName().equals("scheduled-tasks:write"))
                .findFirst()
                .orElseThrow();
        IdentityAwareTestRestTemplate editor =
                restTemplate.withRole(new DefaultRole("SCHEDULED_ONLY", Set.of(authority)));

        // when.
        ResponseEntity<Void> response = editor.postForEntity("/api/external" + path, payload, Void.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(server.getRequestCount()).isZero();
        assertAccessDenied(target);
    }

    static Stream<Arguments> jobsRequests() {
        return Stream.of(JOBS_JSON, "{\"jobs\":[],\"executions\":[]}")
                .flatMap(body -> Stream.of("quartzScheduler", "night shift", "night%20shift")
                        .map(name -> Arguments.of(body, name)));
    }

    static Stream<Arguments> endpointRequests() {
        return Stream.concat(
                Stream.of(
                        Arguments.of("/quartz/{instanceId}", HttpMethod.GET, null),
                        Arguments.of(
                                "/quartz/{instanceId}/jobs",
                                HttpMethod.POST,
                                new QuartzJobsRequest().schedulerName("quartzScheduler")),
                        Arguments.of(
                                "/quartz/{instanceId}/validateCronExpression",
                                HttpMethod.POST,
                                new QuartzCronExpressionValidationRequest().cronExpression("0 0 12 * * ? 2027"))),
                managementRequests()
                        .map(arguments -> Arguments.of(
                                arguments.get()[0], HttpMethod.POST, arguments.get()[1])));
    }

    static Stream<Arguments> managementRequests() {
        return Stream.of(
                Arguments.of(
                        "/quartz/{instanceId}/execute",
                        new QuartzJobExecuteRequest()
                                .schedulerName("quartzScheduler")
                                .jobName("job")
                                .jobGroup("jobs"),
                        MasterWebEndpoints.QUARTZ_JOB_EXECUTE),
                Arguments.of(
                        "/quartz/{instanceId}/pauseJob",
                        new QuartzJobPauseRequest()
                                .schedulerName("quartzScheduler")
                                .jobName("job")
                                .jobGroup("jobs"),
                        MasterWebEndpoints.QUARTZ_JOB_PAUSE),
                Arguments.of(
                        "/quartz/{instanceId}/resumeJob",
                        new QuartzJobResumeRequest()
                                .schedulerName("quartzScheduler")
                                .jobName("job")
                                .jobGroup("jobs"),
                        MasterWebEndpoints.QUARTZ_JOB_RESUME),
                Arguments.of(
                        "/quartz/{instanceId}/pauseTrigger",
                        new QuartzTriggerPauseRequest()
                                .schedulerName("quartzScheduler")
                                .triggerName("cron")
                                .triggerGroup("triggers"),
                        MasterWebEndpoints.QUARTZ_TRIGGER_PAUSE),
                Arguments.of(
                        "/quartz/{instanceId}/resumeTrigger",
                        new QuartzTriggerResumeRequest()
                                .schedulerName("quartzScheduler")
                                .triggerName("cron")
                                .triggerGroup("triggers"),
                        MasterWebEndpoints.QUARTZ_TRIGGER_RESUME));
    }

    static Stream<Arguments> quartzAuthorities() {
        return managementRequests()
                .flatMap(arguments -> Stream.of("EDITOR", "ADMIN")
                        .map(roleName -> Arguments.of(
                                arguments.get()[0],
                                arguments.get()[1],
                                arguments.get()[2],
                                roleName)));
    }

    static Stream<Arguments> managementErrors() {
        return managementRequests()
                .flatMap(arguments -> Stream.of(400, 404, 500)
                        .map(status ->
                                Arguments.of(arguments.get()[0], arguments.get()[1], status)));
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.QUARTZ_SCHEDULERS_READ,
                        "/api/external/quartz/00000000-0000-0000-0000-000000000001"),
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.QUARTZ_JOBS_READ,
                        "/api/external/quartz/00000000-0000-0000-0000-000000000001/jobs"),
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.QUARTZ_JOB_EXECUTE,
                        "/api/external/quartz/00000000-0000-0000-0000-000000000001/execute"),
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.QUARTZ_JOB_PAUSE,
                        "/api/external/quartz/00000000-0000-0000-0000-000000000001/pauseJob"),
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.QUARTZ_JOB_RESUME,
                        "/api/external/quartz/00000000-0000-0000-0000-000000000001/resumeJob"),
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.QUARTZ_TRIGGER_PAUSE,
                        "/api/external/quartz/00000000-0000-0000-0000-000000000001/pauseTrigger"),
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.QUARTZ_TRIGGER_RESUME,
                        "/api/external/quartz/00000000-0000-0000-0000-000000000001/resumeTrigger"),
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.QUARTZ_CRON_EXPRESSION_VALIDATE,
                        "/api/external/quartz/00000000-0000-0000-0000-000000000001/validateCronExpression"),
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.QUARTZ_SIMPLE_TRIGGER_INTERVAL_VALIDATE,
                        "/api/external/quartz/validateSimpleTriggerInterval"));
    }
}
