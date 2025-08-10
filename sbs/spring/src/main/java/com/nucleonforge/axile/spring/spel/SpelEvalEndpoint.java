package com.nucleonforge.axile.spring.spel;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Custom Spring Boot Actuator endpoint for evaluating
 * Spring Expression Language (SpEL).
 *
 * <p>The operation is exposed via an HTTP POST request to the {@code /actuator/spel-eval} path.</p>
 *
 * @since 08.08.2025
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "spel-eval")
public class SpelEvalEndpoint {

    private final SpelEvaluator evaluator;

    public SpelEvalEndpoint(SpelEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @PostMapping
    public SpelEvaluationResponse evaluate(@RequestBody SpelEvaluationRequest request) {
        return evaluator.evaluate(request.spelExpression());
    }
}
