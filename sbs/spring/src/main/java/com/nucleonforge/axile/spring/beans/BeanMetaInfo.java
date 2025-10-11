package com.nucleonforge.axile.spring.beans;

import java.util.List;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.common.api.BeansFeed.ProxyType;

/**
 * Additional bean metadata.
 *
 * @author Mikhail Polivakha
 */
public record BeanMetaInfo(
        ProxyType proxyType,
        boolean isLazyInit,
        boolean isPrimary,
        List<String> qualifiers,
        BeansFeed.BeanSource beanSource) {}
