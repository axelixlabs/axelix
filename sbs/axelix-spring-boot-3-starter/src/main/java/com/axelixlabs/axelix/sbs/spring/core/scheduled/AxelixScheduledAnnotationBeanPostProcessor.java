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

import java.lang.reflect.Method;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.Schedules;

import com.axelixlabs.axelix.sbs.spring.core.scheduled.AxelixScheduledTaskRegistrar.ScheduledAnnotation;

/**
 * {@link ScheduledAnnotationBeanPostProcessor} that gives the task of every {@code @Scheduled} annotation its own id,
 * e.g. {@code com.example.Jobs.sync#cron0}.
 *
 * @author Nikita Kirillov
 */
public class AxelixScheduledAnnotationBeanPostProcessor extends ScheduledAnnotationBeanPostProcessor {

    private final AxelixScheduledTaskRegistrar registrar;

    public AxelixScheduledAnnotationBeanPostProcessor() {
        this(new AxelixScheduledTaskRegistrar());
    }

    private AxelixScheduledAnnotationBeanPostProcessor(AxelixScheduledTaskRegistrar registrar) {
        super(registrar);
        this.registrar = registrar;
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        // looked up lazily, since this processor is created before the regular beans
        registrar.setHistory(() -> beanFactory.getBean(ScheduledTaskExecutionHistory.class));
    }

    @Override
    protected void processScheduled(@NonNull Scheduled scheduled, @NonNull Method method, @NonNull Object bean) {
        ScheduledAnnotation annotation = new ScheduledAnnotation(suffix(scheduled, method), qualifier(scheduled));
        registrar.process(annotation, () -> super.processScheduled(scheduled, method, bean));
    }

    /**
     * @return the kind of the schedule and the ordinal of the annotation among the annotations of the same kind on
     *         the method, in the order the annotations are processed by Spring, e.g. {@code cron1}.
     */
    static String suffix(Scheduled scheduled, Method method) {
        String kind = kind(scheduled);
        int ordinal = 0;
        for (Scheduled other :
                AnnotatedElementUtils.getMergedRepeatableAnnotations(method, Scheduled.class, Schedules.class)) {
            if (other.equals(scheduled)) {
                break;
            }
            if (kind(other).equals(kind)) {
                ordinal++;
            }
        }
        return kind + ordinal;
    }

    private static String kind(Scheduled scheduled) {
        if (!scheduled.cron().isEmpty()) {
            return "cron";
        }
        if (scheduled.fixedDelay() >= 0 || !scheduled.fixedDelayString().isEmpty()) {
            return "fixedDelay";
        }
        if (scheduled.fixedRate() >= 0 || !scheduled.fixedRateString().isEmpty()) {
            return "fixedRate";
        }
        // since Spring Framework 6.1, the annotation with the initial delay only declares a one-time task
        return "oneTime";
    }

    // the attribute exists since Spring Framework 6.1
    private static @Nullable String qualifier(Scheduled scheduled) {
        return AnnotationUtils.getValue(scheduled, "scheduler") instanceof String qualifier ? qualifier : null;
    }
}
