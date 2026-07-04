package org.springframework.samples.petclinic.testdata.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DishRepository extends JpaRepository<Dish, Long> {

}
