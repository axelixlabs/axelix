package com.nucleonforge.axile.common.utils;

/**
 * Utilities to work with bean names.
 *
 * @author Mikhail Polivakha
 */
public class BeanNameUtils {

    /**
     * Strips the configprops prefix from the bean name.
     * <p>
     * The problem is that the bean name of the configprops bean as returned by the actuator, for some reason, contains
     * the dash at the very beginning. I do not know why. We do not want to show it in the bean name.
     */
    public static String stripConfigPropsPrefix(String beanName) {
        int indexOfDash = beanName.indexOf("-");

        if (indexOfDash != -1 && indexOfDash < beanName.length() - 1) {
            return beanName.substring(indexOfDash + 1);
        } else {
            return beanName;
        }
    }
}
