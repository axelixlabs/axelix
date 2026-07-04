INSERT INTO publishers (id, name) VALUES (1, 'Penguin Books'), (2, 'HarperCollins'), (3, 'Random House');

INSERT INTO authors (id, name, publisher_id) VALUES 
(1, 'George Orwell', 1), (2, 'J.R.R. Tolkien', 1),
(3, 'J.K. Rowling', 2), (4, 'George R.R. Martin', 2),
(5, 'Stephen King', 3), (6, 'Agatha Christie', 3);

INSERT INTO books (id, title, author_id) VALUES 
(1, '1984', 1), (2, 'Animal Farm', 1), (3, 'The Hobbit', 2), (4, 'The Silmarillion', 2),
(5, 'Harry Potter and the Philosopher''s Stone', 3), (6, 'Harry Potter and the Chamber of Secrets', 3),
(7, 'A Game of Thrones', 4), (8, 'A Clash of Kings', 4),
(9, 'The Shining', 5), (10, 'It', 5), (11, 'Murder on the Orient Express', 6), (12, 'And Then There Were None', 6);

INSERT INTO editions (id, edition_name, book_id) VALUES 
(1, 'First Edition', 1), (2, 'First Edition', 2), (3, 'First Edition', 3), (4, 'First Edition', 4),
(5, 'First Edition', 5), (6, 'First Edition', 6), (7, 'First Edition', 7), (8, 'First Edition', 8),
(9, 'First Edition', 9), (10, 'First Edition', 10), (11, 'First Edition', 11), (12, 'First Edition', 12);

INSERT INTO reviews (id, title, book_id) VALUES 
(1, 'Critic Review', 1), (2, 'Reader Review', 1), (3, 'Critic Review', 2), (4, 'Reader Review', 2),
(5, 'Critic Review', 3), (6, 'Reader Review', 3), (7, 'Critic Review', 4), (8, 'Reader Review', 4),
(9, 'Critic Review', 5), (10, 'Reader Review', 5), (11, 'Critic Review', 6), (12, 'Reader Review', 6),
(13, 'Critic Review', 7), (14, 'Reader Review', 7), (15, 'Critic Review', 8), (16, 'Reader Review', 8),
(17, 'Critic Review', 9), (18, 'Reader Review', 9), (19, 'Critic Review', 10), (20, 'Reader Review', 10),
(21, 'Critic Review', 11), (22, 'Reader Review', 11), (23, 'Critic Review', 12), (24, 'Reader Review', 12);

INSERT INTO genres (id, name) VALUES (1, 'Dystopian'), (2, 'Fantasy'), (3, 'Horror');

INSERT INTO book_genres (book_id, genre_id) VALUES 
(1, 1), (1, 2), (2, 2), (3, 1), (3, 3), (4, 3),
(5, 1), (6, 2), (7, 3), (8, 1), (9, 2), (10, 3), (11, 1), (12, 2);
