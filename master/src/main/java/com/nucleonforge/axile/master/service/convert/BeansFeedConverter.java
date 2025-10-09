package com.nucleonforge.axile.master.service.convert;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.master.api.response.BeanShortProfile;
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
                            bean.aliases(),
                            bean.dependencies(),
                            bean.isPrimary(),
                            bean.isLazyInit(),
                            bean.qualifiers(),
                            toBeanSource(bean));
                    beansFeedResponse.addBean(profile);
                });
            }
        });

        return beansFeedResponse;
    }

    private BeanShortProfile.BeanSource toBeanSource(BeansFeed.Bean bean) {
        if (bean.enclosingClassName() != null && bean.methodName() != null) {
            return new BeanShortProfile.BeanMethod(bean.enclosingClassName(), bean.methodName());
        } else if (bean.factoryBeanName() != null) {
            return new BeanShortProfile.FactoryBean(bean.factoryBeanName());
        } else if (bean.type() != null && !bean.type().isEmpty()) {
            return new BeanShortProfile.ComponentVariant();
        } else {
            return new BeanShortProfile.UnknownBean();
        }
    }
}
