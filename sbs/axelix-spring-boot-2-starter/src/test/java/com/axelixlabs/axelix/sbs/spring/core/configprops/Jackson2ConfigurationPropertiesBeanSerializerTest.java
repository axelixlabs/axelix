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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import java.time.Duration;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests the observable output of the Jackson 2 configuration properties serializer. */
class Jackson2ConfigurationPropertiesBeanSerializerTest {

    private final ConfigurationPropertiesBeanSerializer serializer =
            new Jackson2ConfigurationPropertiesBeanSerializer();

    @Test
    void preservesNullValuesAndFormatsSpecialTypes() {
        // given.
        SampleProperties bean = new SampleProperties();

        // when.
        Map<String, @Nullable Object> properties = serializer.serialize(bean);

        // then.
        assertThat(properties)
                .containsEntry("unset", null)
                .containsEntry("timeout", "PT5S")
                .containsEntry("size", "2048B")
                .doesNotContainKeys("derived", "self", "broken");
    }

    static class SampleProperties {
        public @Nullable String getUnset() {
            return null;
        }

        public void setUnset(@Nullable String unset) {}

        public Duration getTimeout() {
            return Duration.ofSeconds(5);
        }

        public void setTimeout(Duration timeout) {}

        public DataSize getSize() {
            return DataSize.ofKilobytes(2);
        }

        public void setSize(DataSize size) {}

        public String getDerived() {
            return "not bindable";
        }

        public SampleProperties getSelf() {
            return this;
        }

        public void setSelf(SampleProperties self) {}

        public String getBroken() {
            throw new IllegalStateException();
        }

        public void setBroken(String broken) {}
    }
}
