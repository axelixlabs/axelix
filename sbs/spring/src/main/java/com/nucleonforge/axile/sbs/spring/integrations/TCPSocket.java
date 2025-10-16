package com.nucleonforge.axile.sbs.spring.integrations;

/**
 * Abstraction over tcp socket.
 *
 * @param host peer connection host
 * @param port peer connection port
 *
 * @since 08.07.25
 * @author Mikhail Polivakha
 */
public record TCPSocket(String host, int port) {

    @Override
    public String toString() {
        return "%s:%d".formatted(host, port);
    }
}
