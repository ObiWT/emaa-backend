CREATE TABLE school (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200)
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    school_id INT REFERENCES school(id)
);

CREATE TABLE students (
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
    active BOOLEAN,
    credit INT DEFAULT 0,
    birthdate DATE
);

CREATE TABLE training (
    id SERIAL PRIMARY KEY,
    date DATE NOT NULL,
    school_id INT REFERENCES school(id)
);

CREATE TABLE attendances (
    id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    training_id INT NOT NULL REFERENCES training(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW()
);
