package org.springframework.samples.petclinic.testdata;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A transaction declared on a default method of an interface.
 */
public interface InterfaceRestaurantTestDataService {

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	default void txOnInterfaceDefaultMethod_runNplusOne_1() {
		loadRestaurantsWithChefs();
	}

	void loadRestaurantsWithChefs();

}
