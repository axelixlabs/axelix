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
package org.springframework.samples.petclinic.vet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for {@link VetFeedbackController}, verifying the feature-flag gating
 * and the persistence of feedback for a veterinarian's visit.
 */
class VetFeedbackControllerTests {

	@Nested
	@SpringBootTest(properties = "petclinic.vet.feedback.enabled=true")
	@AutoConfigureMockMvc
	class WhenFeedbackEnabled {

		@Autowired
		private MockMvc mockMvc;

		@Autowired
		private VetVisitFeedbackRepository feedbackRepository;

		@Test
		void savesFeedbackAndReturnsCreated() throws Exception {
			// given.
			long countBefore = feedbackRepository.count();
			String body = "{\"rating\": 5, \"comment\": \"Great visit!\"}";

			// when.
			mockMvc
				.perform(post("/vets/{vetId}/visits/{visitId}/feedback", 1, 1).contentType(MediaType.APPLICATION_JSON)
					.content(body))
				// then.
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.vetId").value(1))
				.andExpect(jsonPath("$.visitId").value(1))
				.andExpect(jsonPath("$.rating").value(5))
				.andExpect(jsonPath("$.comment").value("Great visit!"));

			assertThat(feedbackRepository.count()).isEqualTo(countBefore + 1);
		}

		@Test
		void rejectsInvalidFeedbackWithoutSaving() throws Exception {
			// given.
			long countBefore = feedbackRepository.count();
			String body = "{\"rating\": 9, \"comment\": \"\"}";

			// when.
			mockMvc
				.perform(post("/vets/{vetId}/visits/{visitId}/feedback", 1, 1).contentType(MediaType.APPLICATION_JSON)
					.content(body))
				// then.
				.andExpect(status().isBadRequest());

			assertThat(feedbackRepository.count()).isEqualTo(countBefore);
		}

	}

	@Nested
	@SpringBootTest(properties = "petclinic.vet.feedback.enabled=false")
	@AutoConfigureMockMvc
	class WhenFeedbackDisabled {

		@Autowired
		private MockMvc mockMvc;

		@Autowired
		private VetVisitFeedbackRepository feedbackRepository;

		@Test
		void rejectsFeedbackWithForbiddenAndSavesNothing() throws Exception {
			// given.
			long countBefore = feedbackRepository.count();
			String body = "{\"rating\": 5, \"comment\": \"Great visit!\"}";

			// when.
			mockMvc
				.perform(post("/vets/{vetId}/visits/{visitId}/feedback", 1, 1).contentType(MediaType.APPLICATION_JSON)
					.content(body))
				// then.
				.andExpect(status().isForbidden());

			assertThat(feedbackRepository.count()).isEqualTo(countBefore);
		}

	}

}
