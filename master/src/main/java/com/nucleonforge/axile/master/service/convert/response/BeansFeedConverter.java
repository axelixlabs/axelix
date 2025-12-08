/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.service.convert.response;

import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.common.api.BeansFeed.BeanDependency;
import com.nucleonforge.axile.common.utils.BeanNameUtils;
import com.nucleonforge.axile.master.api.response.BeanShortProfile;
import com.nucleonforge.axile.master.api.response.BeanShortProfile.BeanDependencyProfile;
import com.nucleonforge.axile.master.api.response.BeanShortProfile.BeanMethod;
import com.nucleonforge.axile.master.api.response.BeanShortProfile.BeanSource;
import com.nucleonforge.axile.master.api.response.BeanShortProfile.ComponentVariant;
import com.nucleonforge.axile.master.api.response.BeanShortProfile.FactoryBean;
import com.nucleonforge.axile.master.api.response.BeanShortProfile.ProxyType;
import com.nucleonforge.axile.master.api.response.BeanShortProfile.UnknownBean;
import com.nucleonforge.axile.master.api.response.BeansFeedResponse;

/**
 * The {@link Converter} from {@link BeansFeed} to {@link BeansFeedResponse}.
 *
 * @author Mikhail Polivakha
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
