package com.sivalabs.ft.features.testdata;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactions declared on the class itself.
 */
@Service
@Profile({"default", "local"})
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class TransactionalBookTestDataService {

    private final BookRepository bookRepository;

    public TransactionalBookTestDataService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void txOnClass_runPagination_2(PageRequest page) {
        bookRepository.findAllWithGenresPaged(page);
        TestDataDelays.sleepRandom();
    }
}
