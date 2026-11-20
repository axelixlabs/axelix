CREATE TABLE companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    company_id BIGINT,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    employment_type SMALLINT,
    department_id BIGINT,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

CREATE TABLE employee_projects (
    employee_id BIGINT,
    project_id BIGINT,
    PRIMARY KEY (employee_id, project_id),
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_number VARCHAR(255) NOT NULL,
    employee_id BIGINT UNIQUE,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    employee_id BIGINT,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payee VARCHAR(255) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(16) NOT NULL
);

CREATE TABLE payment_invoices (
    payment_id BIGINT NOT NULL,
    invoice_number VARCHAR(32) NOT NULL,
    PRIMARY KEY (payment_id, invoice_number),
    FOREIGN KEY (payment_id) REFERENCES payments(id)
);

CREATE TABLE risk_flags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL
);

CREATE TABLE payment_risk_flags (
    payment_id BIGINT NOT NULL,
    risk_flag_id BIGINT NOT NULL,
    FOREIGN KEY (payment_id) REFERENCES payments(id),
    FOREIGN KEY (risk_flag_id) REFERENCES risk_flags(id)
);
