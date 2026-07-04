package com.sivalabs.ft.notifications.testdata

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
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
}
