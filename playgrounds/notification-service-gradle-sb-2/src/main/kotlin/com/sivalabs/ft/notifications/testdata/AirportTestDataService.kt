package com.sivalabs.ft.notifications.testdata

import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Service
@Profile("default", "local")
class AirportTestDataService(
    private val airportRepository: AirportRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun runNplusOne1() {
        val airports = airportRepository.findAll()
        airports.forEach { it.flights.size }
        sleepRandom()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun runPagination1(page: PageRequest) {
        airportRepository.findAllWithFlightsPaged(page)
        sleepRandom()
    }

    private fun sleepRandom() {
        val delay = Random.nextLong(10, 50 + 1)
        Thread.sleep(delay)
    }
}
