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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.KotlinDetector;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.util.ClassUtils;

import com.axelixlabs.axelix.common.utils.Lazy;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.AxelixScheduledTaskRegistrar.ScheduledAnnotationContext;

/**
 * {@link ScheduledAnnotationBeanPostProcessor} that gives the task of every {@code @Scheduled} annotation its own id,
 * e.g. {@code com.example.Jobs.sync#cron0}.
 *
 * @author Nikita Kirillov
 */
public class AxelixScheduledAnnotationBeanPostProcessor extends ScheduledAnnotationBeanPostProcessor {

    static final boolean REACTIVE_SUPPORT_PRESENT = ClassUtils.isPresent(
            "org.reactivestreams.Publisher", AxelixScheduledAnnotationBeanPostProcessor.class.getClassLoader());

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
        registrar.setHistory(Lazy.of(() -> beanFactory.getBean(ScheduledTaskExecutionHistory.class)));
    }

    @Override
    protected void processScheduled(Scheduled scheduled, Method method, Object bean) {
        ScheduledAnnotationContext annotation =
                new ScheduledAnnotationContext(suffix(scheduled, method), isReactive(method));
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
        return "oneTime";
    }

    // the same check as Spring Framework does
    private static boolean isReactive(Method method) {
        if (!REACTIVE_SUPPORT_PRESENT) {
            return false;
        }
        if (KotlinDetector.isSuspendingFunction(method)) {
            return true;
        }
        return ReactiveAdapterRegistry.getSharedInstance().getAdapter(method.getReturnType()) != null;
    }
}
