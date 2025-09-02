package com.nucleonforge.axile.spring.spel;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.nucleonforge.axile.spring.spel.context.EvaluationContextProvider;

/**
 * Default implementation of {@link SpelEvaluator}
 *
 * @since 08.08.2025
 * @author Nikita Kirillov
 */
public class DefaultSpelEvaluator implements SpelEvaluator {

    private final EvaluationContext evaluationContext;

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public DefaultSpelEvaluator(EvaluationContextProvider evaluationContextProvider) {
        this.evaluationContext = evaluationContextProvider.initContext();
    }

    @Override
    public SpelEvaluationResponse evaluate(String spelExpression) {
        Object resultExpression;
        try {
            SpelExpression expression = (SpelExpression) expressionParser.parseExpression(spelExpression);

            resultExpression = expression.getValue(evaluationContext);
        } catch (ParseException ex) {
            throw new SpelException("Failed to parse expression: " + spelExpression + ex.getMessage());
        } catch (Exception ex) {
            throw new SpelException("Error evaluating expression: " + ex.getMessage());
        }

        return new SpelEvaluationResponse(resultExpression != null ? resultExpression.toString() : null);
    }
}
