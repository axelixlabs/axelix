package com.nucleonforge.axile.master.service.convert.configprops;

import java.util.List;

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
public class ConfigpropsByPrefixConverter extends AbstractConfigpropsConverter<ConfigpropsByPrefixResponse> {
    @Override
    protected ConfigpropsByPrefixResponse convertBeans(List<ConfigpropsProfile> beans) {
        ConfigpropsByPrefixResponse response = new ConfigpropsByPrefixResponse();
        beans.forEach(response::addBean);
        return response;
    }
}
