package com.nucleonforge.axile.master.service.convert.configprops;

import java.util.List;

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
public class ConfigpropsFeedConverter extends AbstractConfigpropsConverter<ConfigpropsFeedResponse> {

    @Override
    protected ConfigpropsFeedResponse convertBeans(List<ConfigpropsProfile> beans) {
        ConfigpropsFeedResponse response = new ConfigpropsFeedResponse();
        beans.forEach(response::addBean);
        return response;
    }
}
