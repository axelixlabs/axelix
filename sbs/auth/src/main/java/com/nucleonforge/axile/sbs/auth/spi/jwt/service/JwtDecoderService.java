package com.nucleonforge.axile.sbs.auth.spi.jwt.service;

import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.sbs.auth.model.DecodedUser;
import com.nucleonforge.axile.sbs.auth.spi.jwt.exception.ExpiredJwtTokenException;
import com.nucleonforge.axile.sbs.auth.spi.jwt.exception.InvalidJwtTokenException;
import com.nucleonforge.axile.sbs.auth.spi.jwt.exception.JwtParsingException;
import com.nucleonforge.axile.sbs.auth.spi.jwt.exception.JwtTokenDecodingException;

/**
 * Contract for decoding and validating JWT tokens into {@link User} representations.
 *
 * @since 23.07.2025
 * @author Nikita Kirillov
 */
public interface JwtDecoderService {

    /**
     * Parses the given JWT token and converts it into a {@link User}.
     *
     * @param token the JWT token to decode
     * @return the reconstructed {@link User}
     * @throws ExpiredJwtTokenException if the JWT token has expired
     * @throws InvalidJwtTokenException if the JWT token is invalid or tampered with
     * @throws JwtTokenDecodingException if an unexpected error occurs during token decoding
     * @throws JwtParsingException if the token cannot be parsed or contains insufficient data
     */
    DecodedUser decodeTokenToUser(String token)
            throws ExpiredJwtTokenException, InvalidJwtTokenException, JwtTokenDecodingException, JwtParsingException;
}
