package com.nucleonforge.axile.sbs.spring.info;

import jakarta.annotation.Nullable;

public class KotlinVersionProvider {

    public @Nullable String getVersion() {
        try {
            Class<?> versionClass = Class.forName("kotlin.KotlinVersion");
            Object current = versionClass.getField("CURRENT").get(null);
            return current.toString();
        } catch (Throwable ignored) {
            return "";
        }
    }
}
