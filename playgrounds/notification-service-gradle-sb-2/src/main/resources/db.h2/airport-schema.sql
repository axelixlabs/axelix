CREATE TABLE airports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE flights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flight_number VARCHAR(255) NOT NULL,
    status INTEGER NOT NULL,
    airport_id BIGINT,
    FOREIGN KEY (airport_id) REFERENCES airports(id)
);

CREATE TABLE flight_alternate_airports (
    flight_id BIGINT NOT NULL,
    airport_id BIGINT NOT NULL,
    PRIMARY KEY (flight_id, airport_id),
    FOREIGN KEY (flight_id) REFERENCES flights(id),
    FOREIGN KEY (airport_id) REFERENCES airports(id)
);

CREATE TABLE flight_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flight_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (flight_id) REFERENCES flights(id)
);
