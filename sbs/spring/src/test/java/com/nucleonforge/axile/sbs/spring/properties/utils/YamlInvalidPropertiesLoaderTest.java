package com.nucleonforge.axile.sbs.spring.properties.utils;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link YamlInvalidPropertiesLoader}
 *
 * @since 01.12.2025
 * @author Nikita Kirillov
 */
class YamlInvalidPropertiesLoaderTest {

    private final YamlInvalidPropertiesLoader loader =
            new YamlInvalidPropertiesLoader(new DefaultEnvironmentPropertyNameNormalizer());

    @Test
    void shouldLoadAllInvalidPropertiesCorrectly() {
        Map<String, List<InvalidPropertyValue>> result = loader.getInvalidProperties();

        assertThat(result)
                .hasSize(4)
                .containsOnlyKeys(
                        "springjpaopeninview",
                        "springjpapropertieshibernateenablelazyloadnotrans",
                        "springdatasourcehikariautocommit",
                        "springjpahibernateddlauto");

        List<InvalidPropertyValue> openInView = result.get("springjpaopeninview");
        assertThat(openInView)
                .hasSize(1)
                .extracting(InvalidPropertyValue::value)
                .containsExactly(true);
        assertThat(openInView.get(0).description())
                .isEqualTo(
                        "Leaving Open Session in View enabled in production can trigger N+1 queries and unpredictable lazy-loading; should be disabled.");

        List<InvalidPropertyValue> lazyLoad = result.get("springjpapropertieshibernateenablelazyloadnotrans");
        assertThat(lazyLoad).hasSize(1).extracting(InvalidPropertyValue::value).containsExactly(true);
        assertThat(lazyLoad.get(0).description())
                .isEqualTo(
                        "Lazy loading outside an active transaction masks boundary issues and may cause unexpected data access in production; should be disabled.");

        List<InvalidPropertyValue> autoCommit = result.get("springdatasourcehikariautocommit");
        assertThat(autoCommit)
                .hasSize(1)
                .extracting(InvalidPropertyValue::value)
                .containsExactly(true);
        assertThat(autoCommit.get(0).description())
                .isEqualTo(
                        "Enabling auto-commit in production may cause inconsistent transactional behavior and unintended writes; should be disabled.");

        List<InvalidPropertyValue> ddlAuto = result.get("springjpahibernateddlauto");
        assertThat(ddlAuto).hasSize(3);

        assertThat(ddlAuto).extracting(v -> v.value().toString()).containsExactly("create", "create-drop", "update");

        assertThat(ddlAuto)
                .extracting(InvalidPropertyValue::description)
                .containsExactly(
                        "Overridden description from file common-invalid-properties.yml! Should be set validate.",
                        "Overridden description from file common-invalid-properties.yml! Should be set none.",
                        "Overridden description from file common-invalid-properties.yml! Should be set default.");
    }
}
