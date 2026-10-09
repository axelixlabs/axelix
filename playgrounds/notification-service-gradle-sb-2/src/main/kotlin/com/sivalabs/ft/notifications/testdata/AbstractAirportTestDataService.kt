// The method names carry the placement of the transactional annotation, separated by an underscore.
@file:Suppress("ktlint:standard:function-naming")

package com.sivalabs.ft.notifications.testdata

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * A transaction declared on a concrete method of an abstract class.
 */
abstract class AbstractAirportTestDataService(
    protected val flightRepository: FlightRepository,
) {
    // open: a final method cannot be intercepted by the proxy, and the Kotlin Spring plugin opens only the
    // classes annotated with a Spring annotation themselves
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun txOnAbstractClassMethod_runNplusOne2() {
        val flights = flightRepository.findAll()
        flights.forEach { it.airport?.name }
        sleepRandom()
    }
}
