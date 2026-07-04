package org.springframework.samples.petclinic.testdata;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chefs")
public class Chef {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "restaurant_id")
	private Restaurant restaurant;

	@ManyToMany
	@JoinTable(name = "chef_dishes", joinColumns = @JoinColumn(name = "chef_id"),
			inverseJoinColumns = @JoinColumn(name = "dish_id"))
	private Set<Dish> dishes = new HashSet<>();

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public Set<Dish> getDishes() {
		return dishes;
	}

}
