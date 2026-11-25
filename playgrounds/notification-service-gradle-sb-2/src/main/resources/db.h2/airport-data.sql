INSERT INTO airports (id, name) VALUES (1, 'JFK'), (2, 'LAX'), (3, 'BOS'), (4, 'SFO'), (5, 'ORD');

INSERT INTO flights (id, flight_number, airport_id, status) VALUES
(1, 'AA100', 1, 0), (2, 'DL200', 1, 0),
(3, 'UA300', 2, 0), (4, 'SW400', 2, 0),
(5, 'DEMO500', 1, 1);

INSERT INTO flight_alternate_airports (flight_id, airport_id) VALUES
(1, 3), (5, 3), (5, 4), (5, 5);

INSERT INTO flight_plans (id, flight_id) VALUES (1, 1);
