package com.sivalabs.ft.features.testdata;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile({"default", "local"})
public class BookTestDataQueryRunner {

    private final DefaultBookTestDataService bookTestDataService;
    private final TransactionalBookTestDataService transactionalBookTestDataService;

    public BookTestDataQueryRunner(
            DefaultBookTestDataService bookTestDataService,
            TransactionalBookTestDataService transactionalBookTestDataService) {
        this.bookTestDataService = bookTestDataService;
        this.transactionalBookTestDataService = transactionalBookTestDataService;
    }

    @Scheduled(cron = "*/2 * * * * *")
    public void runTests() throws InterruptedException {
        Thread.sleep(10000);
        bookTestDataService.txOnClassMethod_runNplusOne_1();
        bookTestDataService.txOnInterfaceMethod_runNplusOne_2();
        bookTestDataService.txOnParentInterface_runNplusOne_3();
        bookTestDataService.txOnAbstractMethod_runNplusOne_4();

        PageRequest pageRequest = PageRequest.of(0, 2);
        bookTestDataService.txMetaAnnotation_runPagination_1(pageRequest);
        transactionalBookTestDataService.txOnClass_runPagination_2(pageRequest);
    }
}
