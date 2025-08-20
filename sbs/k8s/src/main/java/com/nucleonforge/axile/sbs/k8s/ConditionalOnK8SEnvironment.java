package com.nucleonforge.axile.sbs.k8s;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.cloud.CloudPlatform;

/**
 * Conditional annotation that passes only when the valid K8S environment is detected.
 *
 * @author Mikhail Polivakha
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
public @interface ConditionalOnK8SEnvironment {}
