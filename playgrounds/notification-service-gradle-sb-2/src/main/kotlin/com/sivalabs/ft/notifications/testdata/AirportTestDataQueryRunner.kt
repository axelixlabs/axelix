package com.sivalabs.ft.notifications.testdata

import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("default", "local")
@EnableScheduling
class AirportTestDataQueryRunner(
    private val airportTestDataService: AirportTestDataService,
) {
    @Scheduled(initialDelay = 10000, fixedRate = 60000)
    fun runTests() {
        airportTestDataService.runNplusOne1()

        val pageRequest = PageRequest.of(0, 2)
        airportTestDataService.runPagination1(pageRequest)
    }
}
