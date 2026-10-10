package org.springframework.samples.petclinic.testdata;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A transaction declared on a concrete method of an abstract class.
 */
public abstract class AbstractRestaurantTestDataService {

	protected final ChefRepository chefRepository;

	protected AbstractRestaurantTestDataService(ChefRepository chefRepository) {
		this.chefRepository = chefRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void txOnAbstractClassMethod_runNplusOne_2() {
		List<Chef> chefs = chefRepository.findAll();
		chefs.forEach(c -> c.getDishes().size());

		sleepRandom();
	}

	protected void sleepRandom() {
		try {
			long delay = ThreadLocalRandom.current().nextLong(10, 50 + 1);
			Thread.sleep(delay);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

}
