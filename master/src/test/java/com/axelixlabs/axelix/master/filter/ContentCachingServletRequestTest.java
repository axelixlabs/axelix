/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ContentCachingServletRequest}.
 *
 * @author Mikhail Polivakha
 */
class ContentCachingServletRequestTest {

    @Test
    void shouldCacheBodyAndAllowReReadingThroughInputStream() throws IOException {
        // given.
        String payload = "request-payload";
        var request = new CountingOneShotHttpServletRequest(payload, StandardCharsets.UTF_8);
        var subject = new ContentCachingServletRequest(request);

        // when.
        String firstRead = StreamUtils.copyToString(subject.getInputStream(), StandardCharsets.UTF_8);
        String secondRead = StreamUtils.copyToString(subject.getInputStream(), StandardCharsets.UTF_8);

        // then.
        assertThat(firstRead).isEqualTo(payload);
        assertThat(secondRead).isEqualTo(payload);
        assertThat(request.getInputStreamCalls()).isEqualTo(1);
    }

    @Test
    void shouldCacheBodyAndAllowReaderAfterInputStreamConsumption() throws IOException {
        // given.
        String payload = "reader-content";
        CountingOneShotHttpServletRequest request =
                new CountingOneShotHttpServletRequest(payload, StandardCharsets.UTF_8);
        ContentCachingServletRequest subject = new ContentCachingServletRequest(request);

        // when.
        String streamRead = StreamUtils.copyToString(subject.getInputStream(), StandardCharsets.UTF_8);
        String readerRead = readToString(subject.getReader());

        // then.
        assertThat(streamRead).isEqualTo(payload);
        assertThat(readerRead).isEqualTo(payload);
        assertThat(request.getInputStreamCalls()).isEqualTo(1);
    }

    @Test
    void shouldAllowTwoReaderCalls() throws IOException {
        // given.
        String payload = "reader-content";
        CountingOneShotHttpServletRequest request =
                new CountingOneShotHttpServletRequest(payload, StandardCharsets.UTF_8);
        ContentCachingServletRequest subject = new ContentCachingServletRequest(request);

        // when.
        String streamRead = readToString(subject.getReader());
        String readerRead = readToString(subject.getReader());

        // then.
        assertThat(streamRead).isEqualTo(payload);
        assertThat(readerRead).isEqualTo(payload);
        assertThat(request.getInputStreamCalls()).isEqualTo(1);
    }

    private static final class CountingOneShotHttpServletRequest extends HttpServletRequestWrapper {

        private final OneShotServletInputStream inputStream;
        private final String encoding;
        private int inputStreamCalls;

        private CountingOneShotHttpServletRequest(String payload, Charset charset) {
            super(new MockHttpServletRequest());
            this.encoding = charset.name();
            this.inputStream = new OneShotServletInputStream(payload.getBytes(charset));
        }

        @Override
        public ServletInputStream getInputStream() {
            inputStreamCalls++;
            return inputStream;
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }

        @Override
        public String getCharacterEncoding() {
            return encoding;
        }

        public int getInputStreamCalls() {
            return inputStreamCalls;
        }
    }

    private static String readToString(BufferedReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[256];
        int charsRead;

        while ((charsRead = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, charsRead);
        }

        return builder.toString();
    }

    private static final class OneShotServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        private OneShotServletInputStream(byte[] content) {
            this.inputStream = new ByteArrayInputStream(content);
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
