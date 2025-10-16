package com.nucleonforge.axile.sbs.spring.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.NonNull;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.CacheAwareContextLoaderDelegate;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * {@link TestExecutionListener}, used in integration tests to ensure that
 * the Spring {@link ApplicationContext} remains active after a test completes.
 *
 * <p>If the context is found to be inactive (which may happen due to manual shutdowns,
 * failed restarts, or intentional restarts during testing), this listener attempts to
 * refresh it to avoid cascading test failures or invalid cache reuse.</p>
 *
 * <p>Intended for use in testing scenarios that manipulate the application context
 * (e.g. profile switching, context restarts) and require consistent context availability
 * across test executions.</p>
 *
 * <p>This listener should be registered explicitly via {@code @TestExecutionListeners}.</p>
 *
 * @since 14.07.2025
 * @author Nikita Kirillov
 */
public class ContextKeepAliveTestListener extends AbstractTestExecutionListener {

    private static final Log logger = LogFactory.getLog(ContextKeepAliveTestListener.class);

    @Override
    public void afterTestExecution(@NonNull TestContext testContext) {
        try {
            CacheAwareContextLoaderDelegate cacheAwareContextLoaderDelegate =
                    FieldUtils.getField("cacheAwareContextLoaderDelegate", testContext);
            MergedContextConfiguration mergedContextConfiguration =
                    FieldUtils.getField("mergedContextConfiguration", testContext);
            ApplicationContext context = cacheAwareContextLoaderDelegate.loadContext(mergedContextConfiguration);

            if (context instanceof ConfigurableApplicationContext cac) {
                if (!cac.isActive()) {
                    logger.info("Context is not active, attempting to refresh...");
                    try {
                        cac.refresh();
                        logger.info("Context refreshed successfully");
                    } catch (IllegalStateException e) {
                        logger.info("Context already refreshed, skipping: " + e.getMessage());
                    }
                } else {
                    logger.info("Context is already active");
                }
            }
        } catch (Exception e) {
            logger.error("Failed to refresh application context", e);
        }
    }
}
