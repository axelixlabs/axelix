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

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for leaving feedback about a veterinarian's visit. The endpoint is guarded by
 * the {@code petclinic.vet.feedback.enabled} feature flag exposed through
 * {@link VetFeedbackProperties}.
 */
@RestController
@EnableConfigurationProperties(VetFeedbackProperties.class)
class VetFeedbackController {

	private final VetFeedbackProperties feedbackProperties;

	private final VetVisitFeedbackRepository feedbackRepository;

	VetFeedbackController(VetFeedbackProperties feedbackProperties, VetVisitFeedbackRepository feedbackRepository) {
		this.feedbackProperties = feedbackProperties;
		this.feedbackRepository = feedbackRepository;
	}

	@PostMapping("/vets/{vetId}/visits/{visitId}/feedback")
	public ResponseEntity<VetVisitFeedback> leaveFeedback(@PathVariable int vetId, @PathVariable int visitId,
			@Valid @RequestBody VetVisitFeedbackRequest request) {
		if (!feedbackProperties.isEnabled()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		VetVisitFeedback feedback = new VetVisitFeedback();
		feedback.setVetId(vetId);
		feedback.setVisitId(visitId);
		feedback.setRating(request.rating());
		feedback.setComment(request.comment());

		VetVisitFeedback saved = feedbackRepository.save(feedback);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

}
