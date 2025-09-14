package com.nucleonforge.axile.master.service.convert.configprops;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ConfigpropsFeed;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsFeedResponse;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link ConfigpropsFeed} to {@link ConfigpropsFeedResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ConfigpropsFeedConverter implements Converter<ConfigpropsFeed, ConfigpropsFeedResponse> {

    @Override
    public @NonNull ConfigpropsFeedResponse convertInternal(@NonNull ConfigpropsFeed source) {
        ConfigpropsFeedResponse configpropsFeedResponse = new ConfigpropsFeedResponse();

        source.contexts().values().forEach(context -> context.beans().forEach((beanName, bean) -> {
            ConfigpropsProfile profile =
                    new ConfigpropsProfile(beanName, bean.prefix(), bean.properties(), bean.inputs());
            configpropsFeedResponse.addBean(profile);
        }));

        return configpropsFeedResponse;
    }
}
