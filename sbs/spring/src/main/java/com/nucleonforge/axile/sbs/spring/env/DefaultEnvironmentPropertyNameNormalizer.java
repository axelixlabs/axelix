package com.nucleonforge.axile.sbs.spring.env;

/**
 * Default implementation {@link EnvironmentPropertyNameNormalizer}
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class DefaultEnvironmentPropertyNameNormalizer implements EnvironmentPropertyNameNormalizer {

    @Override
    public String normalize(String propertyName) {
        return propertyName
                .replaceAll("(?<!\\d)0(?!\\d)", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toLowerCase();
    }
}
