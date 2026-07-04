package com.sivalabs.ft.features.testdata;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile({"default", "local"})
public class BookTestDataQueryRunner {

    private final BookTestDataService bookTestDataService;

    public BookTestDataQueryRunner(BookTestDataService bookTestDataService) {
        this.bookTestDataService = bookTestDataService;
    }

    @Scheduled(cron = "*/2 * * * * *")
    public void runTests() throws InterruptedException {
        Thread.sleep(10000);
        bookTestDataService.runNplusOne_1();
        bookTestDataService.runNplusOne_2();
        bookTestDataService.runNplusOne_3();
        bookTestDataService.runNplusOne_4();

        PageRequest pageRequest = PageRequest.of(0, 2);
        bookTestDataService.runPagination_1(pageRequest);
        bookTestDataService.runPagination_2(pageRequest);
    }
}
