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
    private val airportTestDataService: DefaultAirportTestDataService,
    private val transactionalAirportTestDataService: TransactionalAirportTestDataService,
) {
    @Scheduled(initialDelay = 10000, fixedRate = 60000)
    fun runTests() {
        airportTestDataService.txOnClassMethod_runNplusOne1()
        airportTestDataService.txOnAbstractClassMethod_runNplusOne2()

        val pageRequest = PageRequest.of(0, 2)
        airportTestDataService.txOnInterfaceMethod_runPagination1(pageRequest)
        transactionalAirportTestDataService.txOnClass_runPagination2(pageRequest)
    }
}
