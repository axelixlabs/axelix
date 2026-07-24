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
package com.axelixlabs.axelix.sbs.spring.core.master;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DefaultOpenSessionInViewStateProvider}.
 *
 * @author Sergey Cherkasov
 */
class DefaultOpenSessionInViewStateProviderTest {

    @Test
    void returnsDisabled_whenNoOpenSessionInViewBeansPresent() {
        // given.
        var subject = createSubject(new DefaultListableBeanFactory());

        // when.
        boolean enabled = subject.isOpenSessionInViewEnabled();

        // then.
        assertThat(enabled).isFalse();
    }

    @Test
    void returnsEnabled_whenOpenEntityManagerInViewInterceptorPresent() {
        // given.
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("openEntityManagerInViewInterceptor", new OpenEntityManagerInViewInterceptor());
        var subject = createSubject(beanFactory);

        // when.
        boolean enabled = subject.isOpenSessionInViewEnabled();

        // then.
        assertThat(enabled).isTrue();
    }

    @Test
    void returnsEnabled_whenOpenEntityManagerInViewFilterPresent() {
        // given.
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("openEntityManagerInViewFilter", new OpenEntityManagerInViewFilter());
        var subject = createSubject(beanFactory);

        // when.
        boolean enabled = subject.isOpenSessionInViewEnabled();

        // then.
        assertThat(enabled).isTrue();
    }

    private static DefaultOpenSessionInViewStateProvider createSubject(DefaultListableBeanFactory beanFactory) {
        return new DefaultOpenSessionInViewStateProvider(beanFactory);
    }
}
