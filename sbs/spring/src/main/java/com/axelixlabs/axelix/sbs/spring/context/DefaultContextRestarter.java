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
package com.axelixlabs.axelix.sbs.spring.context;

import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;

/**
 * Default implementation of {@link ContextRestarter}.
 *
 * @since 04.07.25
 * @author Mikhail Polivakha
 */
public class DefaultContextRestarter implements ContextRestarter, ApplicationListener<ApplicationSnapshotEvent> {

    private static final Log logger = LogFactory.getLog(DefaultContextRestarter.class);

    @Nullable
    private ConfigurableApplicationContext context;

    @Nullable
    private SpringApplication application;

    private String[] args = new String[0];

    @Override
    public void restartContext() {
        Thread thread = new Thread(this::restartSafely);
        thread.setDaemon(false);
        thread.start();
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationSnapshotEvent event) {
        if (this.context == null) {
            this.context = event.getApplicationContext();
            this.args = event.getArgs();
            this.application = event.getSpringApplication();
            this.application.addInitializers(new PostProcessorInitializer());
        }
    }

    private void restartSafely() {
        try {
            doRestart();
        } catch (Exception e) {
            logger.info("Could not doRestart", e);
        }
    }

    public synchronized void doRestart() {
        if (this.context != null && this.application != null) {
            logger.info("Initiating ApplicationContext restart...");
            this.application.setEnvironment(this.context.getEnvironment());
            close();
            // If running in a webapp then the context classloader is probably going to
            // die, so we need to revert to a safe place before starting again
            overrideClassLoaderForRestart();
            this.context = this.application.run(this.args);
            logger.info("ApplicationContext restarted successfully.");
        } else {
            logger.warn("Cannot restart the ApplicationContext - no snapshot context data received");
        }
    }

    private void close() {
        ApplicationContext context = this.context;
        while (context instanceof Closeable) {
            try {
                ((Closeable) context).close();
            } catch (IOException e) {
                logger.error("Cannot close context: " + context.getId(), e);
            }
            context = context.getParent();
        }
    }

    private void overrideClassLoaderForRestart() {
        if (this.application != null) {
            ClassUtils.overrideThreadContextClassLoader(
                    this.application.getClass().getClassLoader());
        }
    }

    class PostProcessorInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

        @Override
        public void initialize(GenericApplicationContext context) {
            context.registerBean(RetainExistingBeanPostProcessor.class, RetainExistingBeanPostProcessor::new);
        }
    }

    class RetainExistingBeanPostProcessor implements BeanPostProcessor {

        @Override
        public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName)
                throws BeansException {
            if (bean instanceof DefaultContextRestarter) {
                return DefaultContextRestarter.this;
            }
            return bean;
        }
    }
}
