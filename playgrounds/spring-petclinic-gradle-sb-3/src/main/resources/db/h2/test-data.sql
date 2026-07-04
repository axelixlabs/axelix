INSERT INTO companies (id, name) VALUES (1, 'Google'), (2, 'Meta'), (3, 'Apple');

INSERT INTO departments (id, name, company_id) VALUES 
(1, 'Cloud Dev', 1), (2, 'Search Eng', 1),
(3, 'VR Core', 2), (4, 'Ads Platform', 2),
(5, 'iOS System', 3), (6, 'Hardware', 3);

INSERT INTO employees (id, name, department_id) VALUES 
(1, 'Alice', 1), (2, 'Bob', 1), (3, 'Charlie', 2), (4, 'David', 2),
(5, 'Eve', 3), (6, 'Frank', 3), (7, 'Grace', 4), (8, 'Heidi', 4),
(9, 'Ivan', 5), (10, 'Judy', 5), (11, 'Karl', 6), (12, 'Mallory', 6);

INSERT INTO contracts (id, contract_number, employee_id) VALUES 
(1, 'CNT-001', 1), (2, 'CNT-002', 2), (3, 'CNT-003', 3), (4, 'CNT-004', 4),
(5, 'CNT-005', 5), (6, 'CNT-006', 6), (7, 'CNT-007', 7), (8, 'CNT-008', 8),
(9, 'CNT-009', 9), (10, 'CNT-010', 10), (11, 'CNT-011', 11), (12, 'CNT-012', 12);

INSERT INTO documents (id, title, employee_id) VALUES 
(1, 'Passport', 1), (2, 'NDA', 1), (3, 'Passport', 2), (4, 'NDA', 2),
(5, 'Passport', 3), (6, 'NDA', 3), (7, 'Passport', 4), (8, 'NDA', 4),
(9, 'Passport', 5), (10, 'NDA', 5), (11, 'Passport', 6), (12, 'NDA', 6),
(13, 'Passport', 7), (14, 'NDA', 7), (15, 'Passport', 8), (16, 'NDA', 8),
(17, 'Passport', 9), (18, 'NDA', 9), (19, 'Passport', 10), (20, 'NDA', 10),
(21, 'Passport', 11), (22, 'NDA', 11), (23, 'Passport', 12), (24, 'NDA', 12);

INSERT INTO projects (id, title) VALUES (1, 'Project Alpha'), (2, 'Project Beta'), (3, 'Project Gamma');

INSERT INTO employee_projects (employee_id, project_id) VALUES 
(1, 1), (1, 2), (2, 2), (3, 1), (3, 3), (4, 3),
(5, 1), (6, 2), (7, 3), (8, 1), (9, 2), (10, 3), (11, 1), (12, 2);
