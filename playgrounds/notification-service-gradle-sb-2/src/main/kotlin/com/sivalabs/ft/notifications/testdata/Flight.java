package org.springframework.samples.petclinic.testdata.airport;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "flights")
public class Flight {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String flightNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "airport_id")
	private Airport airport;

	public Long getId() { return id; }
	public String getFlightNumber() { return flightNumber; }
}
