package com.sivalabs.ft.notifications.testdata

import kotlin.random.Random

internal fun sleepRandom() {
    val delay = Random.nextLong(10, 50 + 1)
    Thread.sleep(delay)
}
