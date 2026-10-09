// The method names carry the placement of the transactional annotation, separated by an underscore.
@file:Suppress("ktlint:standard:function-naming")

package com.sivalabs.ft.notifications.testdata

import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * A transaction declared on a method of an interface.
 */
interface InterfaceAirportTestDataService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun txOnInterfaceMethod_runPagination1(page: PageRequest)
}
