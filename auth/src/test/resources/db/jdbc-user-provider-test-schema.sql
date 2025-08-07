DROP TABLE IF EXISTS users, roles, authorities, user_roles, role_authorities, role_components CASCADE;

CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE roles (
                       role_id SERIAL PRIMARY KEY,
                       role_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE authorities (
                             authority_id SERIAL PRIMARY KEY,
                             authority_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE user_roles (
                            user_id INT REFERENCES users(user_id),
                            role_id INT REFERENCES roles(role_id),
                            PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_authorities (
                                  role_id INT REFERENCES roles(role_id),
                                  authority_id INT REFERENCES authorities(authority_id),
                                  PRIMARY KEY (role_id, authority_id)
);

CREATE TABLE role_components (
                                 parent_role_id INT REFERENCES roles(role_id),
                                 component_role_id INT REFERENCES roles(role_id),
                                 PRIMARY KEY (parent_role_id, component_role_id)
);