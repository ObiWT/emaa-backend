-- Škola
CREATE TABLE school (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200)
);

-- Používateľ (tréner/admin)
CREATE TABLE user_account (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    school_id INT REFERENCES school(id)
);

-- Študent
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
    vegetarian BOOLEAN,
    active BOOLEAN DEFAULT TRUE,
    credit INT DEFAULT 0,              -- 🔹 aktuálny zostatok kreditu
    payment_type VARCHAR(10) CHECK (payment_type IN ('MONTHLY', 'CREDIT', 'NO_PAYMENT')),  -- 🔹 spôsob platby
    birthdate DATE
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
    amount INT NOT NULL,                      -- kladné = dobitie, záporné = odpočet
    description VARCHAR(200),
    payment_type VARCHAR(20) NOT NULL DEFAULT 'CREDIT',
    created_at TIMESTAMP DEFAULT NOW()
);
