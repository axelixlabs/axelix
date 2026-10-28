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
package com.axelixlabs.axelix.sbs.spring.core.scheduled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AxelixScheduledAnnotationBeanPostProcessor}.
 *
 * @author Nikita Kirillov
 */
class AxelixScheduledAnnotationBeanPostProcessorTest {

    @Test
    void suffix_shouldBeTheKindOfTheScheduleAndOrdinalZeroForSingleAnnotation() {
        assertThat(suffixes("cron")).containsExactly("cron0");
        assertThat(suffixes("fixedDelay")).containsExactly("fixedDelay0");
        assertThat(suffixes("fixedRate")).containsExactly("fixedRate0");
    }

    @Test
    void suffix_shouldCountTheOrdinalWithinTheKindOfTheSchedule() {
        assertThat(suffixes("mixed")).containsExactly("cron0", "fixedDelay0", "cron1", "fixedRate0", "fixedDelay1");
    }

    @Test
    void suffix_shouldTreatTheStringAttributesAsTheSameKindOfTheSchedule() {
        assertThat(suffixes("strings")).containsExactly("fixedDelay0", "fixedRate0", "fixedDelay1");
    }

    @Test
    void suffix_shouldTakeTheComposedAnnotationIntoAccount() {
        assertThat(suffixes("composed")).containsExactly("cron0");
        assertThat(suffixes("composedWithAlias")).containsExactly("fixedDelay0");
    }

    @Test
    void suffix_shouldCountTheComposedAndTheDirectAnnotationsTogether() {
        assertThat(suffixes("composedAndDirect")).containsExactlyInAnyOrder("cron0", "cron1", "fixedDelay0");
    }

    private static List<String> suffixes(String methodName) {
        Method method = ReflectionUtils.findMethod(Jobs.class, methodName);
        assertThat(method).isNotNull();
        return AnnotatedElementUtils.getMergedRepeatableAnnotations(method, Scheduled.class, Schedules.class).stream()
                .map(scheduled -> AxelixScheduledAnnotationBeanPostProcessor.suffix(scheduled, method))
                .collect(Collectors.toList());
    }

    static class Jobs {

        @Scheduled(cron = "0 0 1 * * *")
        void cron() {}

        @Scheduled(fixedDelay = 1000)
        void fixedDelay() {}

        @Scheduled(fixedRate = 1000)
        void fixedRate() {}

        @Scheduled(cron = "0 0 1 * * *")
        @Scheduled(fixedDelay = 1000)
        @Scheduled(cron = "0 0 2 * * *")
        @Scheduled(fixedRate = 1000)
        @Scheduled(fixedDelay = 2000)
        void mixed() {}

        @Scheduled(fixedDelayString = "1000")
        @Scheduled(fixedRateString = "1000")
        @Scheduled(fixedDelay = 2000)
        void strings() {}

        @Nightly
        void composed() {}

        @Every(delay = 1000)
        void composedWithAlias() {}

        @Nightly
        @Scheduled(cron = "0 0 4 * * *")
        @Scheduled(fixedDelay = 1000)
        void composedAndDirect() {}
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Scheduled(cron = "0 0 3 * * *")
    @interface Nightly {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Scheduled
    @interface Every {

        @AliasFor(annotation = Scheduled.class, attribute = "fixedDelay")
        long delay();
    }
}
