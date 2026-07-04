CREATE TABLE publishers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE authors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    publisher_id BIGINT,
    FOREIGN KEY (publisher_id) REFERENCES publishers(id)
);

CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author_id BIGINT,
    FOREIGN KEY (author_id) REFERENCES authors(id)
);

CREATE TABLE genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE book_genres (
    book_id BIGINT,
    genre_id BIGINT,
    PRIMARY KEY (book_id, genre_id),
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (genre_id) REFERENCES genres(id)
);

CREATE TABLE editions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    edition_name VARCHAR(255) NOT NULL,
    book_id BIGINT UNIQUE,
    FOREIGN KEY (book_id) REFERENCES books(id)
);

CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    book_id BIGINT,
    FOREIGN KEY (book_id) REFERENCES books(id)
);
