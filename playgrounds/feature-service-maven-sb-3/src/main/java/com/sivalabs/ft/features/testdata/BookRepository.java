package com.sivalabs.ft.features.testdata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b JOIN FETCH b.reviews")
    Page<Book> findAllWithReviewsPaged(Pageable pageable);

    @Query("SELECT b FROM Book b JOIN FETCH b.genres")
    Page<Book> findAllWithGenresPaged(Pageable pageable);
}
