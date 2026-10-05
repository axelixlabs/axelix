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

import org.springframework.http.client.ClientHttpRequestInterceptor;

/**
 * Attaches the PayGrid API token to every outgoing request so PayGrid can classify the
 * caller. There is deliberately more than one implementation on the classpath; which one
 * is actually in effect depends on the runtime environment - see
 * {@link VaultApiTokenInterceptor} (production) and {@link NoOpApiTokenInterceptor}
 * (test/sandbox).
 */
interface ApiTokenInterceptor extends ClientHttpRequestInterceptor {

}
