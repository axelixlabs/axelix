INSERT INTO restaurants (id, name) VALUES (1, 'La Trattoria'), (2, 'Sushi House');

INSERT INTO chefs (id, name, restaurant_id) VALUES 
(1, 'Mario', 1), (2, 'Luigi', 1),
(3, 'Kenji', 2), (4, 'Hiro', 2);

INSERT INTO dishes (id, name) VALUES (1, 'Pasta Carbonara'), (2, 'Margherita Pizza'), (3, 'Salmon Nigiri');

INSERT INTO chef_dishes (chef_id, dish_id) VALUES 
(1, 1), (1, 2), (2, 2), (3, 3), (4, 3), (4, 1);

INSERT INTO ingredients (id, name, dish_id) VALUES 
(1, 'Pasta', 1), (2, 'Egg', 1),
(3, 'Dough', 2), (4, 'Mozzarella', 2),
(5, 'Salmon', 3), (6, 'Rice', 3);
