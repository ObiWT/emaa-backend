-- Škola
CREATE TABLE school (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    credit_payment INT,
    monthly_payment INT,
    yearly_payment INT
);

-- Používateľ (login do app: admin / inštruktor / ...) 
CREATE TABLE user_account (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    school_id INT REFERENCES school(id), -- NULL pre globálneho admina
    active BOOLEAN DEFAULT TRUE
);

-- Role pre používateľov
CREATE TABLE role (
    id SERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

INSERT INTO role (code, name) VALUES
('ADMIN', 'Administrator'),
('APP_INSTRUCTOR', 'Instructor'),
('APP_STUDENT', 'Student');

-- Prepojenie používateľov a rolí
CREATE TABLE user_role (
    user_id INT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Typ študenta / inštruktora v realite
CREATE TABLE student_type (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(50)
);

INSERT INTO student_type VALUES
('STUDENT', 'Student'),
('INSTRUCTOR', 'Instructor');

-- Študent (doménová entita)
CREATE TABLE student (
    id SERIAL PRIMARY KEY,
    firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    gender CHAR(1),
    id_card VARCHAR(50),
    street VARCHAR(100),
    street_no VARCHAR(20),
    city VARCHAR(100),
    zip_code VARCHAR(20),
    mobil VARCHAR(20),
    email VARCHAR(100),
    school_id INT REFERENCES school(id),
    vegetarian BOOLEAN DEFAULT FALSE,
    gluten_free BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    credit INT DEFAULT 0,
    payment_type VARCHAR(10) CHECK (payment_type IN ('MONTHLY', 'YEARLY', 'CREDIT', 'NO_PAYMENT')),
    base_payment_amount INT,
    grade INT,
    birthdate DATE,
    created_at TIMESTAMP DEFAULT NOW(),
    national_id VARCHAR(50),
    student_type VARCHAR(20) REFERENCES student_type(code)
);

-- Tréning
CREATE TABLE training (
    id SERIAL PRIMARY KEY,
    date DATE NOT NULL,
    school_id INT REFERENCES school(id)
);

-- Dochádzka
CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    training_id INT NOT NULL REFERENCES training(id) ON DELETE CASCADE,
    present BOOLEAN
);

-- História platobných transakcií (dobitia / odpočty)
CREATE TABLE credit_transaction (
    id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    amount INT NOT NULL,                      
    description VARCHAR(200),
    payment_type VARCHAR(20) NOT NULL DEFAULT 'CREDIT',
    created_at TIMESTAMP DEFAULT NOW(),
    training_id INT REFERENCES training(id) ON DELETE CASCADE    
);