package com.sivalabs.ft.features.testdata;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"default", "local"})
public class BookTestDataService {

    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public BookTestDataService(
            PublisherRepository publisherRepository, AuthorRepository authorRepository, BookRepository bookRepository) {
        this.publisherRepository = publisherRepository;
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_1() {
        List<Publisher> publishers = publisherRepository.findAll();
        publishers.forEach(p -> p.getAuthors().size());

        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_2() {
        List<Author> authors = authorRepository.findAll();
        authors.forEach(a -> a.getBooks().size());

        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_3() {
        List<Book> books = bookRepository.findAll();
        books.forEach(b -> b.getReviews().size());

        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_4() {
        List<Book> books = bookRepository.findAll();
        books.forEach(b -> b.getGenres().size());

        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runPagination_1(PageRequest page) {
        bookRepository.findAllWithReviewsPaged(page);

        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runPagination_2(PageRequest page) {
        bookRepository.findAllWithGenresPaged(page);
        sleepRandom();
    }

    private void sleepRandom() {
        try {
            long delay = ThreadLocalRandom.current().nextLong(10, 50 + 1);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
