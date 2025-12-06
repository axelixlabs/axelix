package com.nucleonforge.axile.sbs.spring.env;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Default implementation {@link PropertyNameNormalizer}.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class DefaultPropertyNameNormalizer implements PropertyNameNormalizer {

    @Override
    public String normalize(String propertyName) {
        return propertyName
                .replaceAll("(?<!\\d)0(?!\\d)", "") // removes the zero index like [0] --> []
                .replaceAll("[^A-Za-z0-9]", "")
                .toLowerCase();
    }

    @Override
    public <C extends Collection<String>> C normalizeAll(C propertyNames, Supplier<C> collectionFactory) {
        return propertyNames.stream().map(this::normalize).collect(Collectors.toCollection(collectionFactory));
    }
}
