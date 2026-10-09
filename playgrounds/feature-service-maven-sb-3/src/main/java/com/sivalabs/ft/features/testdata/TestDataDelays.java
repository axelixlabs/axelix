package com.sivalabs.ft.features.testdata;

import java.util.concurrent.ThreadLocalRandom;

final class TestDataDelays {

    private TestDataDelays() {}

    static void sleepRandom() {
        try {
            long delay = ThreadLocalRandom.current().nextLong(10, 50 + 1);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
