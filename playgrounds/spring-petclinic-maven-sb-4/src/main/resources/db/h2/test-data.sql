INSERT INTO companies (id, name) VALUES (1, 'Google'), (2, 'Meta'), (3, 'Apple');

INSERT INTO departments (id, name, company_id) VALUES 
(1, 'Cloud Dev', 1), (2, 'Search Eng', 1),
(3, 'VR Core', 2), (4, 'Ads Platform', 2),
(5, 'iOS System', 3), (6, 'Hardware', 3);

INSERT INTO employees (id, name, department_id) VALUES 
(1, 'Alice', 1), (2, 'Bob', 1), (3, 'Charlie', 2), (4, 'David', 2),
(5, 'Eve', 3), (6, 'Frank', 3), (7, 'Grace', 4), (8, 'Heidi', 4),
(9, 'Ivan', 5), (10, 'Judy', 5), (11, 'Karl', 6), (12, 'Mallory', 6);

-- Demo employee without dependent rows, with projects shared by other employees.
INSERT INTO employees (id, name, department_id, employment_type) VALUES (13, 'Taylor', 1, 1);

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
(5, 1), (6, 2), (7, 3), (8, 1), (9, 2), (10, 3), (11, 1), (12, 2), (13, 1), (13, 2), (13, 3);

INSERT INTO payments (id, payee, amount, currency, status) VALUES 
(1, 'Acme Veterinary Supplies', 157.17, 'EUR', 'PENDING'), (2, 'Northwind Pharma', 194.34, 'EUR', 'PENDING'),
(3, 'Globex Diagnostics', 231.51, 'EUR', 'PENDING'), (4, 'Initech Lab Services', 268.68, 'EUR', 'PENDING'),
(5, 'Umbrella Pet Foods', 305.85, 'EUR', 'PENDING'), (6, 'Stark Medical Devices', 342.02, 'EUR', 'PENDING'),
(7, 'Wayne Animal Insurance', 379.19, 'EUR', 'PENDING'), (8, 'Hooli Cloud Hosting', 416.36, 'EUR', 'PENDING'),
(9, 'Soylent Nutrition', 453.53, 'EUR', 'PENDING'), (10, 'Wonka Treats', 490.70, 'EUR', 'PENDING'),
(11, 'Cyberdyne Imaging', 527.87, 'EUR', 'PENDING'), (12, 'Tyrell Genetics', 564.04, 'EUR', 'PENDING'),
(13, 'Acme Veterinary Supplies', 601.21, 'EUR', 'PROCESSED'), (14, 'Northwind Pharma', 638.38, 'EUR', 'PROCESSED'),
(15, 'Globex Diagnostics', 675.55, 'EUR', 'PROCESSED'), (16, 'Initech Lab Services', 712.72, 'EUR', 'PROCESSED');

INSERT INTO payment_invoices (payment_id, invoice_number) VALUES 
(1, 'INV-2026-0001'), (1, 'INV-2026-0002'), (2, 'INV-2026-0003'), (3, 'INV-2026-0004'),
(3, 'INV-2026-0005'), (4, 'INV-2026-0006'), (5, 'INV-2026-0007'), (5, 'INV-2026-0008'),
(6, 'INV-2026-0009'), (7, 'INV-2026-0010'), (7, 'INV-2026-0011'), (8, 'INV-2026-0012'),
(9, 'INV-2026-0013'), (9, 'INV-2026-0014'), (10, 'INV-2026-0015'), (11, 'INV-2026-0016'),
(11, 'INV-2026-0017'), (12, 'INV-2026-0018'), (13, 'INV-2026-0019'), (13, 'INV-2026-0020'),
(14, 'INV-2026-0021'), (15, 'INV-2026-0022'), (15, 'INV-2026-0023'), (16, 'INV-2026-0024');

INSERT INTO risk_flags (id, code) VALUES
(1, 'LARGE_AMOUNT'), (2, 'NEW_PAYEE'), (3, 'NIGHT_TIME'), (4, 'FOREIGN_CURRENCY');

INSERT INTO payment_risk_flags (payment_id, risk_flag_id) VALUES
(1, 2), (2, 1), (2, 3), (4, 2), (6, 1), (7, 1), (7, 2), (9, 3), (11, 1), (12, 1), (12, 3);
