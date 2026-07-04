package com.sivalabs.ft.notifications.testdata

import org.springframework.data.jpa.repository.JpaRepository

interface FlightRepository : JpaRepository<Flight, Long>
