package com.nucleonforge.axile.master.config.graalvm;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.api.env.PropertyValue;
import com.nucleonforge.axile.common.api.info.ServiceInfo;
import com.nucleonforge.axile.common.api.info.components.BuildInfo;
import com.nucleonforge.axile.common.api.info.components.GitInfo;
import com.nucleonforge.axile.common.api.info.components.JavaInfo;
import com.nucleonforge.axile.common.api.info.components.OSInfo;
import com.nucleonforge.axile.common.api.info.components.ProcessInfo;
import com.nucleonforge.axile.common.api.info.components.SSLInfo;

/**
 * Configuration class for registering classes for reflection.
 *
 * <p>Ensures that specified classes are available for reflection at runtime,
 * when building a GraalVM native image.</p>
 *
 * @since 08.09.2025
 * @author Nikita Kirillov
 */
@Configuration
@RegisterReflectionForBinding({
    // Beans
    BeansFeed.class,
    BeansFeed.Context.class,
    BeansFeed.Bean.class,

    // Env
    EnvironmentFeed.class,
    EnvironmentFeed.PropertySource.class,
    PropertyValue.class,

    // Info
    ServiceInfo.class,
    BuildInfo.class,
    GitInfo.class,
    JavaInfo.class,
    OSInfo.class,
    ProcessInfo.class,
    SSLInfo.class
})
public class CommonApiReflectionConfig {}
