
DROP TABLE IF EXISTS customers;
CREATE TABLE customers (
    id SERIAL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    age INT,
    course VARCHAR(255),
    password VARCHAR(255),
    dataCreated TIMESTAMP,
    dataUpdated TIMESTAMP
);