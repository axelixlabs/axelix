package org.springframework.samples.petclinic.testdata.airport;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "airports")
public class Airport {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;

	@OneToMany(mappedBy = "airport")
	private Set<Flight> flights = new HashSet<>();

	public Long getId() { return id; }
	public String getName() { return name; }
	public Set<Flight> getFlights() { return flights; }
}
