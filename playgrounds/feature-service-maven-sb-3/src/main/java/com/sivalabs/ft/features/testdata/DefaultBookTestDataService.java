package com.sivalabs.ft.features.testdata;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactions declared on methods: of the class itself, of its interface and of its abstract parent.
 */
@Service
@Profile({"default", "local"})
public class DefaultBookTestDataService extends AbstractBookTestDataService implements InterfaceBookTestDataService {

    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public DefaultBookTestDataService(
            PublisherRepository publisherRepository, AuthorRepository authorRepository, BookRepository bookRepository) {
        this.publisherRepository = publisherRepository;
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void txOnClassMethod_runNplusOne_1() {
        List<Publisher> publishers = publisherRepository.findAll();
        publishers.forEach(p -> p.getAuthors().size());

        TestDataDelays.sleepRandom();
    }

    @Override
    public void txOnInterfaceMethod_runNplusOne_2() {
        List<Author> authors = authorRepository.findAll();
        authors.forEach(a -> a.getBooks().size());

        TestDataDelays.sleepRandom();
    }

    @Override
    public void txOnParentInterface_runNplusOne_3() {
        List<Book> books = bookRepository.findAll();
        books.forEach(b -> b.getReviews().size());

        TestDataDelays.sleepRandom();
    }

    @Override
    public void txOnAbstractMethod_runNplusOne_4() {
        List<Book> books = bookRepository.findAll();
        books.forEach(b -> b.getGenres().size());

        TestDataDelays.sleepRandom();
    }

    @RequiresNewTransactional
    public void txMetaAnnotation_runPagination_1(PageRequest page) {
        bookRepository.findAllWithReviewsPaged(page);

        TestDataDelays.sleepRandom();
    }
}
