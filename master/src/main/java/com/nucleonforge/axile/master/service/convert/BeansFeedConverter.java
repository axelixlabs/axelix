package com.nucleonforge.axile.master.service.convert;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.master.api.response.BeanShortProfile;
import com.nucleonforge.axile.master.api.response.BeanShortProfile.BeanMethod;
import com.nucleonforge.axile.master.api.response.BeanShortProfile.BeanSource;
import com.nucleonforge.axile.master.api.response.BeanShortProfile.ComponentVariant;
import com.nucleonforge.axile.master.api.response.BeanShortProfile.FactoryBean;
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
                    BeanShortProfile profile = new BeanShortProfile(
                            beanName,
                            bean.scope(),
                            bean.type(),
                            BeanShortProfile.ProxyType.valueOf(bean.proxyType().name()),
                            bean.aliases(),
                            bean.dependencies(),
                            bean.isPrimary(),
                            bean.isLazyInit(),
                            bean.qualifiers(),
                            covertBeanSource(bean));
                    beansFeedResponse.addBean(profile);
                });
            }
        });

        return beansFeedResponse;
    }

    private static BeanSource covertBeanSource(BeansFeed.Bean bean) {
        BeansFeed.BeanSource beanSource = bean.beanSource();

        // TODO: migrate to switch over the sealed interface on java 21
        return switch (beanSource.origin()) {
            case COMPONENT_ANNOTATION -> new ComponentVariant();
            case BEAN_METHOD ->
                new BeanMethod(
                        ((BeansFeed.BeanMethod) beanSource).enclosingClassName(),
                        ((BeansFeed.BeanMethod) beanSource).methodName());
            case FACTORY_BEAN -> new FactoryBean(((BeansFeed.FactoryBean) beanSource).factoryBeanName());
            case UNKNOWN -> new UnknownBean();
        };
    }
}
