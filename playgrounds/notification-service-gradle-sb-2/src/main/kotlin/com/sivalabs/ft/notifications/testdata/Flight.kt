package com.sivalabs.ft.notifications.testdata

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "flights")
class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var flightNumber: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airport_id")
    var airport: Airport? = null

    @ManyToMany(cascade = [CascadeType.REMOVE])
    @JoinTable(
        name = "flight_alternate_airports",
        joinColumns = [JoinColumn(name = "flight_id")],
        inverseJoinColumns = [JoinColumn(name = "airport_id")],
    )
    var alternateAirports: MutableList<Airport> = mutableListOf()

    @OneToOne(mappedBy = "flight", fetch = FetchType.LAZY)
    var flightPlan: FlightPlan? = null

    @Enumerated(EnumType.ORDINAL)
    var status: Status = Status.SCHEDULED

    override fun toString(): String = "Flight(id=$id, flightNumber=$flightNumber, airport=$airport, alternateAirports=$alternateAirports)"

    enum class Status {
        SCHEDULED,
        DEPARTED,
        CANCELLED,
    }
}
