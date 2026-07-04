CREATE TABLE airports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE flights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flight_number VARCHAR(255) NOT NULL,
    airport_id BIGINT,
    FOREIGN KEY (airport_id) REFERENCES airports(id)
);
