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

        int len = propertyName.length();
        StringBuilder propertyNameNormalizer = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            char c = Character.toLowerCase(propertyName.charAt(i));
            if (Character.isLetterOrDigit(c)) {
                propertyNameNormalizer.append(c);
            }
        }

        return propertyNameNormalizer.toString();
    }
}
