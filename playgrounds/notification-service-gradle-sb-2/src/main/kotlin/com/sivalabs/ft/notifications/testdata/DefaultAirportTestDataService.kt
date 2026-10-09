// The method names carry the placement of the transactional annotation, separated by an underscore.
@file:Suppress("ktlint:standard:function-naming")

package com.sivalabs.ft.notifications.testdata

import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * Transactions declared on methods: of the class itself, of its interface and of its abstract parent.
 */
@Service
@Profile("default", "local")
class DefaultAirportTestDataService(
    private val airportRepository: AirportRepository,
    flightRepository: FlightRepository,
) : AbstractAirportTestDataService(flightRepository),
    InterfaceAirportTestDataService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun txOnClassMethod_runNplusOne1() {
        val airports = airportRepository.findAll()
        airports.forEach { it.flights.size }
        sleepRandom()
    }

    override fun txOnInterfaceMethod_runPagination1(page: PageRequest) {
        airportRepository.findAllWithFlightsPaged(page)
        sleepRandom()
    }
}
