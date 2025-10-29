package com.nucleonforge.axile.master.service.convert.details;

import org.jspecify.annotations.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.details.components.CustomInfo;
import com.nucleonforge.axile.master.api.response.details.components.SpringProfile;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.service.convert.Converter;

@Service
public class SpringConverter implements Converter<CustomInfo, SpringProfile> {

    @Autowired
    private Instance instance;

    @Override
    public @NonNull SpringProfile convertInternal(@NonNull CustomInfo source) {
        return new SpringProfile(
                instance.springBootVersion(), source.springFrameworkVersion(), source.springCloudVersion());
    }
}
