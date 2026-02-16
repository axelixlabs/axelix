/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.beans;

import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.api.BeansFeed;
import com.axelixlabs.axelix.common.api.BeansFeed.ProxyType;

/**
 * Additional bean metadata.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public final class BeanMetaInfo {

    @Nullable
    private final String autoConfigurationRef;

    private final ProxyType proxyType;
    private final boolean isLazyInit;
    private final boolean isPrimary;
    private final List<String> qualifiers;
    private final BeansFeed.BeanSource beanSource;

    public BeanMetaInfo(
            @Nullable String autoConfigurationRef,
            ProxyType proxyType,
            boolean isLazyInit,
            boolean isPrimary,
            List<String> qualifiers,
            BeansFeed.BeanSource beanSource) {
        this.autoConfigurationRef = autoConfigurationRef;
        this.proxyType = proxyType;
        this.isLazyInit = isLazyInit;
        this.isPrimary = isPrimary;
        this.qualifiers = qualifiers;
        this.beanSource = beanSource;
    }

    @Nullable
    public String getAutoConfigurationRef() {
        return autoConfigurationRef;
    }

    public ProxyType getProxyType() {
        return proxyType;
    }

    public boolean isLazyInit() {
        return isLazyInit;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public List<String> getQualifiers() {
        return qualifiers;
    }

    public BeansFeed.BeanSource getBeanSource() {
        return beanSource;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (BeanMetaInfo) obj;
        return Objects.equals(this.autoConfigurationRef, that.autoConfigurationRef)
                && Objects.equals(this.proxyType, that.proxyType)
                && this.isLazyInit == that.isLazyInit
                && this.isPrimary == that.isPrimary
                && Objects.equals(this.qualifiers, that.qualifiers)
                && Objects.equals(this.beanSource, that.beanSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(autoConfigurationRef, proxyType, isLazyInit, isPrimary, qualifiers, beanSource);
    }

    @Override
    public String toString() {
        return "BeanMetaInfo[" + "autoConfigurationRef="
                + autoConfigurationRef + ", " + "proxyType="
                + proxyType + ", " + "isLazyInit="
                + isLazyInit + ", " + "isPrimary="
                + isPrimary + ", " + "qualifiers="
                + qualifiers + ", " + "beanSource="
                + beanSource + ']';
    }
}
