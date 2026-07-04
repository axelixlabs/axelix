package org.springframework.samples.petclinic.testdata.bookstore;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class BookTestDataQueryRunner {

    private final BookTestDataService bookTestDataService;

    public BookTestDataQueryRunner(BookTestDataService bookTestDataService) {
        this.bookTestDataService = bookTestDataService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runTests() {
        bookTestDataService.runNplusOne_1();
        bookTestDataService.runNplusOne_2();
        bookTestDataService.runNplusOne_3();
        bookTestDataService.runNplusOne_4();

        PageRequest pageRequest = PageRequest.of(0, 2);
        bookTestDataService.runPagination_1(pageRequest);
        bookTestDataService.runPagination_2(pageRequest);
    }
}
