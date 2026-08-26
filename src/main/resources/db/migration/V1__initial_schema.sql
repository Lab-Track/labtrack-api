CREATE TABLE professor (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE student (
    id BIGSERIAL PRIMARY KEY,
    registration_number VARCHAR(50) UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    reliability_rate NUMERIC(5,2) NOT NULL DEFAULT 100,
    registration_date TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE technician (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    login VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE project (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    professor_id BIGINT NOT NULL REFERENCES professor(id)
);

CREATE TABLE equipment (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    identification_photo VARCHAR(500) NOT NULL,
    current_status VARCHAR(20) NOT NULL
        CHECK (current_status IN ('available', 'loaned', 'maintenance', 'damaged')),
    project_id BIGINT REFERENCES project(id)
);

CREATE TABLE status_history (
    id BIGSERIAL PRIMARY KEY,
    equipment_id BIGINT NOT NULL REFERENCES equipment(id),
    previous_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    change_date TIMESTAMP NOT NULL DEFAULT now(),
    technician_id BIGINT NOT NULL REFERENCES technician(id)
);

CREATE TABLE loan (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES student(id),
    responsible_professor_id BIGINT NOT NULL REFERENCES professor(id),
    technician_id BIGINT NOT NULL REFERENCES technician(id),
    checkout_date TIMESTAMP NOT NULL,
    expected_return_date TIMESTAMP NOT NULL,
    extended_date TIMESTAMP,
    loan_status VARCHAR(20) NOT NULL
        CHECK (loan_status IN ('in_progress', 'late', 'returned', 'partial'))
);

CREATE TABLE loan_item (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loan(id),
    equipment_id BIGINT NOT NULL REFERENCES equipment(id),
    checkout_photo VARCHAR(500) NOT NULL,
    checkout_condition VARCHAR(255) NOT NULL,
    item_status VARCHAR(20) NOT NULL
        CHECK (item_status IN ('loaned', 'returned', 'removed'))
);

CREATE TABLE loan_return (
    id BIGSERIAL PRIMARY KEY,
    loan_item_id BIGINT NOT NULL UNIQUE REFERENCES loan_item(id),
    return_date TIMESTAMP NOT NULL,
    return_condition VARCHAR(255) NOT NULL,
    notes VARCHAR(1000),
    verification_status VARCHAR(20) NOT NULL
        CHECK (verification_status IN ('working', 'defective', 'not_tested')),
    overdue BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loan(id),
    sent_date TIMESTAMP NOT NULL,
    channel VARCHAR(50) NOT NULL,
    delivery_status VARCHAR(20) NOT NULL
);
