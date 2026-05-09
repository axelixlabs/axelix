package com.axelixlabs.axelix.master.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * This is {@link jakarta.servlet.http.HttpServletRequest} that is really caching the
 * content of the {@link java.io.InputStream}. We cannot use Spring's {@link ContentCachingRequestWrapper},
 * since it is not working as many might expect in terms of caching.
 * <p>
 * This class, as opposed to {@link ContentCachingRequestWrapper}, returns a caching InputStream/Reader,
 * and thus anyone who calls {@code getInputStream} or {@code getReader} can be sure that it can re-read
 * the request
 *
 * @author Mikhail Polivakha
 */
public class ContentCachingServletRequest extends HttpServletRequestWrapper {

    private byte[] cachedContent;
    private boolean contentLoaded;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public ContentCachingServletRequest(HttpServletRequest request) {
        super(request);
        this.cachedContent = new byte[0];
        this.contentLoaded = false;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedServletInputStream(getContentAsByteArray());
    }

    @Override
    public BufferedReader getReader() throws IOException {
        String characterEncoding = getCharacterEncoding();
        String effectiveEncoding = characterEncoding == null ? StandardCharsets.UTF_8.name() : characterEncoding;
        return new BufferedReader(new InputStreamReader(getInputStream(), effectiveEncoding));
    }

    private byte[] getContentAsByteArray() throws IOException {
        if (!contentLoaded) {
            cachedContent = super.getInputStream().readAllBytes();
            contentLoaded = true;
        }

        return cachedContent;
    }

    private static final class CachedServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        private CachedServletInputStream(byte[] cachedContent) {
            this.inputStream = new ByteArrayInputStream(cachedContent);
        }

        @Override
        public int read() {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            // no-op
        }
    }
}
