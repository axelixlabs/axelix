/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.payments;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.autoconfigure.RestClientSsl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wires the {@link RestClient} used to talk to the external PayGrid payment provider. The
 * base URL comes from the {@code paygrid.host} property (resolved per datacenter), the
 * client certificate from the {@code paygrid} SSL bundle (PayGrid requires mutual TLS),
 * and the API token from whichever {@link ApiTokenInterceptor} is in effect for the
 * current environment.
 */
@Configuration(proxyBeanMethods = false)
class PayGridClientConfig {

	@Bean
	RestClient payGridRestClient(RestClient.Builder builder, RestClientSsl ssl, ApiTokenInterceptor tokenInterceptor,
			@Value("${paygrid.host}") String host) {
		return builder.baseUrl(host).apply(ssl.fromBundle("paygrid")).requestInterceptor(tokenInterceptor).build();
	}

}
