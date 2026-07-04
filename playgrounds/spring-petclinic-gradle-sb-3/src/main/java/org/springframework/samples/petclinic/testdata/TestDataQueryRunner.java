package org.springframework.samples.petclinic.testdata;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile({"default", "local"})
public class TestDataQueryRunner {

    private final TestDataService testDataService;

    public TestDataQueryRunner(TestDataService testDataService) {
        this.testDataService = testDataService;
    }

    @Scheduled(initialDelay = 10000, fixedRate = 60000)
    public void runTests() {
        testDataService.runNplusOne_1();
        testDataService.runNplusOne_2();
        testDataService.runNplusOne_3();
        testDataService.runNplusOne_4();
        testDataService.runNplusOne_5();
        testDataService.runNplusOne_6();
        testDataService.runNplusOne_7();

        PageRequest pageRequest = PageRequest.of(0, 2);
        testDataService.runPagination_1(pageRequest);
        testDataService.runPagination_2(pageRequest);
        testDataService.runPagination_3(pageRequest);
        testDataService.runPagination_4(pageRequest);
    }
}
