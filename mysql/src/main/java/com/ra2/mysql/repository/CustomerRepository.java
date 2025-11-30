package com.ra2.mysql.repository;

import com.ra2.mysql.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class CustomerRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper per a convertir resultats de SQL en objectes Customer
    private static final class CustomerRowMapper implements RowMapper<Customer> {
        @Override
        public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
            Customer customer = new Customer();
            customer.setId(rs.getLong("id"));
            customer.setName(rs.getString("name"));
            customer.setDescription(rs.getString("description"));
            customer.setAge(rs.getInt("age"));
            customer.setCourse(rs.getString("course"));
            customer.setPassword(rs.getString("password"));
            customer.setImagePath(rs.getString("image_path"));
            customer.setDataCreated(rs.getTimestamp("dataCreated"));
            customer.setDataUpdated(rs.getTimestamp("dataUpdated"));

            return customer;
        }
    }

    // Funció per obtenir tots els customers
    public List<Customer> findAll() {
        return jdbcTemplate.query("SELECT * FROM customers", new CustomerRowMapper());
    }

    // Funció per afegir un customer rebut desde el body de la petició
    public void addCustomer(Customer customer) {
        // Afegim les dates actuals
        Timestamp now = new Timestamp(System.currentTimeMillis());
        customer.setDataCreated(now);
        customer.setDataUpdated(now);

        // Insertem a la bbdd
        jdbcTemplate.update(
                "INSERT INTO customers (name, description, age, course, password, image_path, dataCreated, dataUpdated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                customer.getName(),
                customer.getDescription(),
                customer.getAge(),
                customer.getCourse(),
                customer.getPassword(),
                customer.getImagePath(),
                customer.getDataCreated(),
                customer.getDataUpdated()
        );
    }

    // Funció per obtenir un customer per ID
    public Customer findById(Long id) {
        List<Customer> customers = jdbcTemplate.query(
                "SELECT * FROM customers WHERE id = ?",
                new Object[]{id},
                new CustomerRowMapper()
        );

        // Si la llista està buida, retornem null
        if (customers.isEmpty()) {
            return null;
        } else {
            return customers.get(0); // Retornem el resultat
        }
    }

    // Funció per actualitzar un customer existent
    public void updateCustomer(Customer customer) {
        jdbcTemplate.update(
                "UPDATE customers SET name = ?, description = ?, age = ?, course = ?, password = ?, image_path = ?, dataUpdated = ? WHERE id = ?",
                customer.getName(),
                customer.getDescription(),
                customer.getAge(),
                customer.getCourse(),
                customer.getPassword(),
                customer.getImagePath(),
                customer.getDataUpdated(),
                customer.getId()
        );
    }

    // Funció per actualitzar només l'age d'un customer
    public void updateCustomerAge(Customer customer) {
        jdbcTemplate.update(
                "UPDATE customers SET age = ?, dataUpdated = ? WHERE id = ?",
                customer.getAge(),
                customer.getDataUpdated(),
                customer.getId()
        );
    }

    // Funció per eliminar un customer per ID
    public void deleteCustomer(Long id) {
        jdbcTemplate.update("DELETE FROM customers WHERE id = ?", id);
    }

    // Funció per actualitzar el camp image_path d'un customer
    public void updateCustomerImage(Long id, String imagePath) {
        // Actualitza el camp image_path i dataUpdated
        jdbcTemplate.update(
                "UPDATE customers SET image_path = ?, dataUpdated = CURRENT_TIMESTAMP WHERE id = ?",
                imagePath,
                id
        );
    }
}
