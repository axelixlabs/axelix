package com.nucleonforge.axile.common.api;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import com.nucleonforge.axile.common.api.BeansFeed.BeanSource;

/**
 * Custom {@link JsonDeserializer} for the {@link BeanSource}.
 *
 * @author Nikita Kirillov
 */
public class BeanSourceDeserializer extends JsonDeserializer<BeanSource> {

    public static final String ORIGIN_FIELD = "origin";

    @Override
    public BeanSource deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);

        return switch (BeansFeed.BeanOrigin.valueOf(node.get(ORIGIN_FIELD).asText())) {
            case COMPONENT_ANNOTATION -> new BeansFeed.ComponentVariant();
            case BEAN_METHOD -> ctxt.readTreeAsValue(node, BeansFeed.BeanMethod.class);
            case FACTORY_BEAN -> ctxt.readTreeAsValue(node, BeansFeed.FactoryBean.class);
            case UNKNOWN -> new BeansFeed.UnknownBean();
        };
    }
}
