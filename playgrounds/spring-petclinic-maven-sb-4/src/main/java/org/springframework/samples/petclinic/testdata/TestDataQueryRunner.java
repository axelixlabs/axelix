package org.springframework.samples.petclinic.testdata;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile({ "default", "local" })
public class TestDataQueryRunner {

	private final DefaultTestDataService testDataService;

	private final TransactionalInterfaceTestDataService transactionalInterfaceTestDataService;

	private final TransactionalTestDataService transactionalTestDataService;

	private final TransactionalAbstractTestDataService transactionalAbstractTestDataService;

	public TestDataQueryRunner(DefaultTestDataService testDataService,
			TransactionalInterfaceTestDataService transactionalInterfaceTestDataService,
			TransactionalTestDataService transactionalTestDataService,
			TransactionalAbstractTestDataService transactionalAbstractTestDataService) {
		this.testDataService = testDataService;
		this.transactionalInterfaceTestDataService = transactionalInterfaceTestDataService;
		this.transactionalTestDataService = transactionalTestDataService;
		this.transactionalAbstractTestDataService = transactionalAbstractTestDataService;
	}

	@Scheduled(initialDelay = 10000, fixedRate = 60000)
	public void runTests() {
		testDataService.txOnClassMethod_runNplusOne_1();
		testDataService.txOnInterfaceMethod_runNplusOne_2();
		transactionalInterfaceTestDataService.txOnInterface_runNplusOne_3();
		testDataService.txOnParentInterface_runNplusOne_4();
		testDataService.txOnInterfaceDefaultMethod_runNplusOne_5();
		testDataService.txOnAbstractClassMethod_runNplusOne_6();
		testDataService.txOnAbstractMethod_runNplusOne_7();

		PageRequest pageRequest = PageRequest.of(0, 2);
		transactionalTestDataService.txOnClass_runPagination_1(pageRequest);
		transactionalAbstractTestDataService.txOnAbstractClass_runPagination_2(pageRequest);
		testDataService.txMetaAnnotation_runPagination_3(pageRequest);
		testDataService.txJakarta_runPagination_4(pageRequest);
	}

}
