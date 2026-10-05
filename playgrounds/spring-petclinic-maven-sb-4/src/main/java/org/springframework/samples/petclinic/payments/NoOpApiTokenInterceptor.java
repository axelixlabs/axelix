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

import org.springframework.context.annotation.Fallback;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

/**
 * Test / sandbox token interceptor: it attaches no token at all, because PayGrid
 * sandboxes do not require one. Annotated {@link Fallback @Fallback} so it is only used
 * when no environment-specific implementation (such as {@link VaultApiTokenInterceptor})
 * is registered.
 */
@Component
@Fallback
class NoOpApiTokenInterceptor implements ApiTokenInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		// No token is attached - sandboxes don't require one.
		return execution.execute(request, body);
	}

}
