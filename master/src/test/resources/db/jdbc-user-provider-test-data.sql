INSERT INTO users (username) VALUES ('adminUser');
INSERT INTO users (username) VALUES ('basicUser');
INSERT INTO users (username) VALUES ('nonexistentAuthorityUser');

INSERT INTO roles (role_name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (role_name) VALUES ('ROLE_ENGINEER');
INSERT INTO roles (role_name) VALUES ('ROLE_USER');
INSERT INTO roles (role_name) VALUES ('ROLE_CACHE_DISPATCHER');
INSERT INTO roles (role_name) VALUES ('ROLE_CACHE_ACCESS');
INSERT INTO roles (role_name) VALUES ('ROLE_BEANS_ACCESS');
INSERT INTO roles (role_name) VALUES ('ROLE_WITH_NONEXISTENT_AUTHORITY');

INSERT INTO authorities (authority_name) VALUES ('PROFILE_MANAGEMENT');
INSERT INTO authorities (authority_name) VALUES ('BEANS');
INSERT INTO authorities (authority_name) VALUES ('ENV');
INSERT INTO authorities (authority_name) VALUES ('INFO');
INSERT INTO authorities (authority_name) VALUES ('CACHE_DISPATCHER');
INSERT INTO authorities (authority_name) VALUES ('CACHES');
INSERT INTO authorities (authority_name) VALUES ('NONEXISTENT_AUTHORITY');

INSERT INTO user_roles (user_id, role_id)
VALUES
    ((SELECT user_id FROM users WHERE username = 'adminUser'), (SELECT role_id FROM roles WHERE role_name = 'ROLE_ADMIN')),
    ((SELECT user_id FROM users WHERE username = 'basicUser'), (SELECT role_id FROM roles WHERE role_name = 'ROLE_BEANS_ACCESS')),
    ((SELECT user_id FROM users WHERE username = 'nonexistentAuthorityUser'), (SELECT role_id FROM roles WHERE role_name = 'ROLE_WITH_NONEXISTENT_AUTHORITY'));

INSERT INTO role_authorities (role_id, authority_id)
VALUES
    ((SELECT role_id FROM roles WHERE role_name = 'ROLE_ADMIN'), (SELECT authority_id FROM authorities WHERE authority_name = 'PROFILE_MANAGEMENT')),
    ((SELECT role_id FROM roles WHERE role_name = 'ROLE_ENGINEER'), (SELECT authority_id FROM authorities WHERE authority_name = 'ENV')),
    ((SELECT role_id FROM roles WHERE role_name = 'ROLE_USER'), (SELECT authority_id FROM authorities WHERE authority_name = 'INFO')),
    ((SELECT role_id FROM roles WHERE role_name = 'ROLE_CACHE_DISPATCHER'), (SELECT authority_id FROM authorities WHERE authority_name = 'CACHE_DISPATCHER')),
    ((SELECT role_id FROM roles WHERE role_name = 'ROLE_CACHE_ACCESS'), (SELECT authority_id FROM authorities WHERE authority_name = 'CACHES')),
    ((SELECT role_id FROM roles WHERE role_name = 'ROLE_BEANS_ACCESS'), (SELECT authority_id FROM authorities WHERE authority_name = 'BEANS')),
    ((SELECT role_id FROM roles WHERE role_name = 'ROLE_WITH_NONEXISTENT_AUTHORITY'), (SELECT authority_id FROM authorities WHERE authority_name = 'NONEXISTENT_AUTHORITY'));

INSERT INTO role_components (parent_role_id, component_role_id)
VALUES
    ((SELECT role_id FROM roles WHERE role_name = 'ROLE_ADMIN'), (SELECT role_id FROM roles WHERE role_name = 'ROLE_ENGINEER')),
    ((SELECT role_id FROM roles WHERE role_name = 'ROLE_ENGINEER'), (SELECT role_id FROM roles WHERE role_name = 'ROLE_USER')),
    ((SELECT role_id FROM roles WHERE role_name = 'ROLE_ADMIN'), (SELECT role_id FROM roles WHERE role_name = 'ROLE_CACHE_DISPATCHER')),
    ((SELECT role_id FROM roles WHERE role_name = 'ROLE_CACHE_DISPATCHER'), (SELECT role_id FROM roles WHERE role_name = 'ROLE_CACHE_ACCESS'));
