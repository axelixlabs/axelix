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
package com.axelixlabs.axelix.sbs.spring.core.persistence.kafka;

import org.jspecify.annotations.NonNull;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.kafka.core.KafkaTemplate;

import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;

/**
 * {@link BeanPostProcessor} that wraps every {@link KafkaTemplate} bean with a proxy carrying the
 * {@link ExternalCallKafkaSendInterceptor}, so that producer sends performed while a transaction is open are
 * recorded as external calls.
 *
 * @author Sergey Cherkasov
 */
public class KafkaTemplateMonitoringBeanPostProcessor implements BeanPostProcessor {

    private final TransactionAccessor transactionAccessor;

    public KafkaTemplateMonitoringBeanPostProcessor(TransactionAccessor transactionAccessor) {
        this.transactionAccessor = transactionAccessor;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (!(bean instanceof KafkaTemplate<?, ?> kafkaTemplate) || isAlreadyInstrumented(bean)) {
            return bean;
        }

        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice(new ExternalCallKafkaSendInterceptor(transactionAccessor, kafkaTemplate));

        return proxyFactory.getProxy(kafkaTemplate.getClass().getClassLoader());
    }

    private static boolean isAlreadyInstrumented(Object bean) {
        if (bean instanceof Advised advised) {
            for (Advisor advisor : advised.getAdvisors()) {
                if (advisor.getAdvice() instanceof ExternalCallKafkaSendInterceptor) {
                    return true;
                }
            }
        }

        return false;
    }
}
