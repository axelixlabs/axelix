package org.springframework.samples.petclinic.testdata.bookstore;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {}
