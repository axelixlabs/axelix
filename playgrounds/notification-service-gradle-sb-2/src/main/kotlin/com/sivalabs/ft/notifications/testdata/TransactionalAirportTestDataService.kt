// The method names carry the placement of the transactional annotation, separated by an underscore.
@file:Suppress("ktlint:standard:function-naming")

package com.sivalabs.ft.notifications.testdata

import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * Transactions declared on the class itself.
 */
@Service
@Profile("default", "local")
@Transactional(propagation = Propagation.REQUIRES_NEW)
class TransactionalAirportTestDataService(
    private val airportRepository: AirportRepository,
) {
    fun txOnClass_runPagination2(page: PageRequest) {
        airportRepository.findAllWithFlightsPaged(page)
        sleepRandom()
    }
}
