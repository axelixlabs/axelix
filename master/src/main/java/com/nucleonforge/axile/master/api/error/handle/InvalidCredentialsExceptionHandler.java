package com.nucleonforge.axile.master.api.error.handle;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.error.ApiError;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.exception.auth.InvalidCredentialsException;

/**
 * The exception handler for the {@link InvalidCredentialsException}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class InvalidCredentialsExceptionHandler implements ExceptionHandler<InvalidCredentialsException> {

    private static final String INVALID_CREDENTIALS_CODE = "INVALID_CREDENTIALS";

    @Override
    public ResponseEntity<ApiError> handle(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SimpleApiError(INVALID_CREDENTIALS_CODE));
    }

    @Override
    public Class<InvalidCredentialsException> supported() {
        return InvalidCredentialsException.class;
    }
}
