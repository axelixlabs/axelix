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

        BeansFeed.Context context =
                source.getContexts().values().stream().findFirst().orElse(null);

        BeansFeedResponse beansFeedResponse = new BeansFeedResponse();

        if (context == null) {
            return beansFeedResponse;
        }

        context.getBeans().forEach((beanName, bean) -> {
            BeanShortProfile profile = new BeanShortProfile(
                    beanName, bean.getScope(), bean.getType(), bean.getAliases(), bean.getDependencies());

            beansFeedResponse.addBean(profile);
        });

        return beansFeedResponse;
    }
}
