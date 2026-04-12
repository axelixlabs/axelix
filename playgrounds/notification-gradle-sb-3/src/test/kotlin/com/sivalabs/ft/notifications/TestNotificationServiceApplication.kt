package com.sivalabs.ft.notifications

import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<NotificationServiceApplication>().with(TestcontainersConfiguration::class).run(*args)
}
