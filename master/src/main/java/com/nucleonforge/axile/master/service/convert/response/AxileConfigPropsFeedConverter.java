package com.nucleonforge.axile.master.service.convert.response;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.AxileConfigPropsFeed;
import com.nucleonforge.axile.master.api.response.configprops.ConfigPropsFeedResponse;
import com.nucleonforge.axile.master.api.response.configprops.ConfigPropsProfile;

/**
 * The {@link Converter} from {@link AxileConfigPropsFeed} to {@link ConfigPropsFeedResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class AxileConfigPropsFeedConverter implements Converter<AxileConfigPropsFeed, ConfigPropsFeedResponse> {

    @Override
    public @NonNull ConfigPropsFeedResponse convertInternal(@NonNull AxileConfigPropsFeed source) {
        ConfigPropsFeedResponse response = new ConfigPropsFeedResponse();

        source.contexts().values().forEach(context -> {
            if (context != null && context.beans() != null) {
                context.beans()
                        .forEach((beanName, bean) -> response.addBean(
                                new ConfigPropsProfile(beanName, bean.prefix(), bean.properties(), bean.inputs())));
            }
        });

        return response;
    }
}
