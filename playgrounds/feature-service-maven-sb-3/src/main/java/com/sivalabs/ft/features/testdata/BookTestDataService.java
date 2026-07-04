package org.springframework.samples.petclinic.testdata.bookstore;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
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
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_2() {
        List<Author> authors = authorRepository.findAll();
        authors.forEach(a -> a.getBooks().size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_3() {
        List<Book> books = bookRepository.findAll();
        books.forEach(b -> b.getReviews().size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_4() {
        List<Book> books = bookRepository.findAll();
        books.forEach(b -> b.getGenres().size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runPagination_1(PageRequest page) {
        bookRepository.findAllWithReviewsPaged(page);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runPagination_2(PageRequest page) {
        bookRepository.findAllWithGenresPaged(page);
    }
}
