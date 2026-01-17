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
package com.nucleonforge.axelix.master.service.convert.response;

import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axelix.common.api.BeansFeed;
import com.nucleonforge.axelix.common.api.BeansFeed.BeanDependency;
import com.nucleonforge.axelix.common.utils.BeanNameUtils;
import com.nucleonforge.axelix.master.api.response.BeanShortProfile;
import com.nucleonforge.axelix.master.api.response.BeanShortProfile.BeanDependencyProfile;
import com.nucleonforge.axelix.master.api.response.BeanShortProfile.BeanMethod;
import com.nucleonforge.axelix.master.api.response.BeanShortProfile.BeanSource;
import com.nucleonforge.axelix.master.api.response.BeanShortProfile.ComponentVariant;
import com.nucleonforge.axelix.master.api.response.BeanShortProfile.FactoryBean;
import com.nucleonforge.axelix.master.api.response.BeanShortProfile.ProxyType;
import com.nucleonforge.axelix.master.api.response.BeanShortProfile.UnknownBean;
import com.nucleonforge.axelix.master.api.response.BeansFeedResponse;

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

        source.contexts().values().forEach(context -> {
            if (context != null && context.beans() != null) {
                context.beans().forEach((beanName, bean) -> {
                    boolean isConfigPropsBean = bean.isConfigPropsBean();
                    String processedBeanName =
                            isConfigPropsBean ? BeanNameUtils.stripConfigPropsPrefix(beanName) : beanName;

                    BeanShortProfile profile = new BeanShortProfile(
                            processedBeanName,
                            bean.scope(),
                            bean.type(),
                            ProxyType.valueOf(bean.proxyType().name()),
                            bean.aliases(),
                            bean.autoConfigurationRef(),
                            convertDependencies(bean.dependencies()),
                            bean.isPrimary(),
                            bean.isLazyInit(),
                            isConfigPropsBean,
                            bean.qualifiers(),
                            covertBeanSource(bean));
                    beansFeedResponse.addBean(profile);
                });
            }
        });

        return beansFeedResponse;
    }

    private Set<BeanDependencyProfile> convertDependencies(Set<BeanDependency> dependencies) {
        return dependencies.stream()
                .map(dep -> {
                    boolean isConfigPropsDep = dep.isConfigPropsDependency();
                    String processedDepName =
                            isConfigPropsDep ? BeanNameUtils.stripConfigPropsPrefix(dep.name()) : dep.name();

                    return new BeanDependencyProfile(processedDepName, isConfigPropsDep);
                })
                .collect(Collectors.toSet());
    }

    private static BeanSource covertBeanSource(BeansFeed.Bean bean) {
        BeansFeed.BeanSource beanSource = bean.beanSource();

        // TODO: migrate to switch over the sealed interface on java 21
        return switch (beanSource.origin()) {
            case COMPONENT_ANNOTATION -> new ComponentVariant();
            case BEAN_METHOD ->
                new BeanMethod(
                        ((BeansFeed.BeanMethod) beanSource).enclosingClassName(),
                        ((BeansFeed.BeanMethod) beanSource).enclosingClassFullName(),
                        ((BeansFeed.BeanMethod) beanSource).methodName());
            case FACTORY_BEAN -> new FactoryBean(((BeansFeed.FactoryBean) beanSource).factoryBeanName());
            case SYNTHETIC_BEAN -> new BeanShortProfile.SyntheticBean();
            case UNKNOWN -> new UnknownBean();
        };
    }
}
