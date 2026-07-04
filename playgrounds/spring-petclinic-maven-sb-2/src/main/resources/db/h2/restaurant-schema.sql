CREATE TABLE restaurants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE chefs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    restaurant_id BIGINT,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

CREATE TABLE dishes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE chef_dishes (
    chef_id BIGINT,
    dish_id BIGINT,
    PRIMARY KEY (chef_id, dish_id),
    FOREIGN KEY (chef_id) REFERENCES chefs(id),
    FOREIGN KEY (dish_id) REFERENCES dishes(id)
);

CREATE TABLE ingredients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    dish_id BIGINT,
    FOREIGN KEY (dish_id) REFERENCES dishes(id)
);
