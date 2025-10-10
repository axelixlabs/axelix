package com.nucleonforge.axile.common.api;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class BeanSourceDeserializer extends JsonDeserializer<BeansFeed.BeanSource> {

    @Override
    public BeansFeed.BeanSource deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);

        return switch (BeansFeed.BeanOrigin.valueOf(node.get("origin").asText())) {
            case COMPONENT_ANNOTATION -> new BeansFeed.ComponentVariant();
            case BEAN_METHOD -> ctxt.readTreeAsValue(node, BeansFeed.BeanMethod.class);
            case FACTORY_BEAN -> ctxt.readTreeAsValue(node, BeansFeed.FactoryBean.class);
            case UNKNOWN -> new BeansFeed.UnknownBean();
        };
    }
}
