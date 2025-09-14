package com.nucleonforge.axile.master.service.convert.configprops;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ConfigpropsFeed;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsByPrefixResponse;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link ConfigpropsFeed} to {@link ConfigpropsByPrefixResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ConfigpropsByPrefixConverter implements Converter<ConfigpropsFeed, ConfigpropsByPrefixResponse> {

    @Override
    public @NonNull ConfigpropsByPrefixResponse convertInternal(@NonNull ConfigpropsFeed source) {
        ConfigpropsByPrefixResponse configpropsByPrefixResponse = new ConfigpropsByPrefixResponse();

        source.contexts().values().forEach(context -> context.beans().forEach((beanName, bean) -> {
            ConfigpropsProfile profile =
                    new ConfigpropsProfile(beanName, bean.prefix(), bean.properties(), bean.inputs());
            configpropsByPrefixResponse.addBean(profile);
        }));

        return configpropsByPrefixResponse;
    }
}
