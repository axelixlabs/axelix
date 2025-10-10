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
                            BeanShortProfile.ProxyType.valueOf(bean.proxyType().name()),
                            bean.aliases(),
                            bean.dependencies(),
                            bean.isPrimary(),
                            bean.isLazyInit(),
                            bean.qualifiers(),
                            new BeanShortProfile.BeanMethod("a","a"));
                    beansFeedResponse.addBean(profile);
                });
            }
        });

        return beansFeedResponse;
    }
}
