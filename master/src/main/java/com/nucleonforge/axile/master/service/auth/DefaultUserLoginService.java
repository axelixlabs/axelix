package com.nucleonforge.axile.master.service.auth;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.master.exception.auth.AuthenticationException;
import com.nucleonforge.axile.master.exception.auth.InvalidCredentialsException;
import com.nucleonforge.axile.master.service.auth.jwt.JwtEncoderService;
import com.nucleonforge.axile.master.service.auth.provider.UserProvider;

/**
 * Default {@link UserLoginService}.
 *
 * @author Mikhail Polivakha
 */
@Service
public class DefaultUserLoginService implements UserLoginService {

    private final JwtEncoderService jwtEncoderService;
    private final UserProvider userProvider;

    private static final InvalidCredentialsException INVALID_CREDENTIALS_EXCEPTION = new InvalidCredentialsException();

    protected DefaultUserLoginService(JwtEncoderService jwtEncoderService, UserProvider userProvider) {
        this.jwtEncoderService = jwtEncoderService;
        this.userProvider = userProvider;
    }

    @Override
    public String login(String username, String password) throws AuthenticationException {
        User user = userProvider.load(username);

        // TODO: password should be encoded. We need a password encoder or something
        if (Objects.equals(user.password(), password)) {
            return jwtEncoderService.generateToken(user);
        } else {
            throw INVALID_CREDENTIALS_EXCEPTION;
        }
    }
}
