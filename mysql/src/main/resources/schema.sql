
DROP TABLE IF EXISTS customers;
CREATE TABLE customers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    age INT,
    course VARCHAR(255),
    password VARCHAR(255),
    image_path VARCHAR(500) NULL,
    dataCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dataUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);