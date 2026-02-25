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
import { EBeanOrigin, EProxyType } from "models";

/**
 * Response for the /beans endpoint
 */
export interface IBeansResponseBody {
    /**
     * The list of beans
     */
    beans: IBean[];
}

export interface IDependency {
    /**
     * The name of the dependent bean
     */
    name: string;

    /**
     * flag that designates that the bean is the config props bean.
     */
    isConfigPropsDependency: boolean;
}

/**
 * An interface that represents the state of the particular bean inside the Spring Boot application
 */
export interface IBean {
    /**
     * Name of single bean
     */
    beanName: string;

    /**
     * Name of single bean scope
     */
    scope: string;

    /**
     * Name of single bean class name
     */
    className: string;

    /**
     * The proxying algorithm that is used for this bean. Might be null in case
     * the backend was unable to figure it out.
     */
    proxyType: EProxyType | null;

    /**
     * Qualified of this bean, if any
     */
    qualifiers: string[];

    /**
     * Whether this bean is lazily initialized
     */
    isLazyInit: boolean;

    /**
     * Whether this bean is marked as primary
     */
    isPrimary: boolean;

    /**
     * Bean aliases list
     */
    aliases: string[];

    /**
     * Bean dependencies
     */
    dependencies: IDependency[];

    /**
     * The source from which the bean came from.
     */
    beanSource: IBeanSource;

    /**
     * flag that designates that the bean is the config props bean.
     */
    isConfigPropsBean: boolean;

    /**
     * Reference to autoconfiguration (or to the conditional class/bean for that matter) from which the
     * bean has come from. If the given bean did not come from @Conditional-like annotated class/method,
     * then this field is null.
     */
    autoConfigurationRef: string | null;
}

/**
 * The "source" of the bean. By "source" we mean how exactly this particular {@link IBean} was discovered
 * by the Spring Framework
 */
export interface IBeanSource {
    /**
     * Optional name of the method that actually produces the instance of the given bean. Present in response from the
     * backend only in case of {@link origin} is equal to {@link BeanOrigin.BEAN_METHOD}
     */
    methodName?: string;

    /**
     * Optional fully qualified name of the enclosing @Configuration class from which the bean came from. Not
     * to be confused with {@link IBean.className}. Present in response from the backend only in case of {@link origin}
     * is equal to {@link BeanOrigin.BEAN_METHOD}
     */
    enclosingClassName?: string;

    /**
     * Full name of the enclosing class’s name
     */
    enclosingClassFullName?: string;

    /**
     * Optional name of the factory bean name. Present in response from the server only if
     * {@link origin} is equal to {@link BeanOrigin.FACTORY_BEAN}
     */
    factoryBeanName?: string;

    /**
     * The actual origin
     */
    origin: EBeanOrigin;
}
