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

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

/**
 * Production token interceptor: it attaches the real bearer token (sourced from Vault) to
 * every PayGrid request. Registered only when {@code environment=production}; for any
 * other value of the {@code environment} property the condition does not match, this bean
 * is skipped, and {@link NoOpApiTokenInterceptor} takes over instead.
 */
@Component
@ConditionalOnProperty(name = "environment", havingValue = "production")
class VaultApiTokenInterceptor implements ApiTokenInterceptor {

	private final String apiToken;

	VaultApiTokenInterceptor(@Value("${paygrid.vault.token:}") String apiToken) {
		this.apiToken = apiToken;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		request.getHeaders().setBearerAuth(this.apiToken);
		return execution.execute(request, body);
	}

}
