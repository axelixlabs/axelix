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
package com.axelixlabs.axelix.master.service.convert.response;

import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.BeansFeed;
import com.axelixlabs.axelix.common.api.BeansFeed.BeanDependency;
import com.axelixlabs.axelix.master.api.external.response.BeanShortProfile;
import com.axelixlabs.axelix.master.api.external.response.BeanShortProfile.BeanDependencyProfile;
import com.axelixlabs.axelix.master.api.external.response.BeanShortProfile.BeanMethod;
import com.axelixlabs.axelix.master.api.external.response.BeanShortProfile.BeanSource;
import com.axelixlabs.axelix.master.api.external.response.BeanShortProfile.ComponentVariant;
import com.axelixlabs.axelix.master.api.external.response.BeanShortProfile.FactoryBean;
import com.axelixlabs.axelix.master.api.external.response.BeanShortProfile.ProxyType;
import com.axelixlabs.axelix.master.api.external.response.BeanShortProfile.UnknownBean;
import com.axelixlabs.axelix.master.api.external.response.BeansFeedResponse;

/**
 * The {@link Converter} from {@link BeansFeed} to {@link BeansFeedResponse}.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@Service
public class BeansFeedConverter implements Converter<BeansFeed, BeansFeedResponse> {

    @Override
    public @NonNull BeansFeedResponse convertInternal(@NonNull BeansFeed source) {
        BeansFeedResponse beansFeedResponse = new BeansFeedResponse();

        source.getContexts().values().forEach(context -> {
            if (context != null && context.getBeans() != null) {
                context.getBeans().forEach((beanName, bean) -> {
                    BeanShortProfile profile = new BeanShortProfile(
                            beanName,
                            bean.getScope(),
                            bean.getType(),
                            ProxyType.valueOf(bean.getProxyType().name()),
                            bean.getAliases(),
                            bean.getAutoConfigurationRef(),
                            convertDependencies(bean.getDependencies()),
                            bean.isPrimary(),
                            bean.isLazyInit(),
                            bean.isConfigPropsBean(),
                            bean.getQualifiers(),
                            covertBeanSource(bean));
                    beansFeedResponse.addBean(profile);
                });
            }
        });

        return beansFeedResponse;
    }

    private Set<BeanDependencyProfile> convertDependencies(Set<BeanDependency> dependencies) {
        return dependencies.stream()
                .map(dep -> new BeanDependencyProfile(dep.getName(), dep.isConfigPropsDependency()))
                .collect(Collectors.toSet());
    }

    private static BeanSource covertBeanSource(BeansFeed.Bean bean) {
        BeansFeed.BeanSource beanSource = bean.getBeanSource();

        // TODO: migrate to switch over the sealed interface on java 21
        return switch (beanSource.origin()) {
            case COMPONENT_ANNOTATION -> new ComponentVariant();
            case BEAN_METHOD ->
                new BeanMethod(
                        ((BeansFeed.BeanMethod) beanSource).getEnclosingClassName(),
                        ((BeansFeed.BeanMethod) beanSource).getEnclosingClassFullName(),
                        ((BeansFeed.BeanMethod) beanSource).getMethodName());
            case FACTORY_BEAN -> new FactoryBean(((BeansFeed.FactoryBean) beanSource).getFactoryBeanName());
            case SYNTHETIC_BEAN -> new BeanShortProfile.SyntheticBean();
            case UNKNOWN -> new UnknownBean();
        };
    }
}
