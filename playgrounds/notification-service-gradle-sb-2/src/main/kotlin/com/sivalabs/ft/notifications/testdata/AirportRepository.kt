package com.sivalabs.ft.notifications.testdata

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface AirportRepository : JpaRepository<Airport, Long> {
    @Query(
        value = "SELECT a FROM Airport a JOIN FETCH a.flights",
        countQuery = "SELECT COUNT(a) FROM Airport a",
    )
    fun findAllWithFlightsPaged(pageable: Pageable): Page<Airport>
}
