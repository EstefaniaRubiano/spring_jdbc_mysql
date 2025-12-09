package com.ra2.mysql.repository;

import com.ra2.mysql.logging.CustomLogging;
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
    @Autowired
    private CustomLogging customLogging;

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
        customLogging.logInfo("CustomerRepository", "findAll", "Executant consulta: SELECT * FROM customers");
        try {
            return jdbcTemplate.query("SELECT * FROM customers", new CustomerRowMapper());
        } catch (Exception e) {
            customLogging.logError("CustomerRepository", "findAll", "Error executant consulta SELECT * FROM customers", e);
            throw e;
        }
    }

    // Funció per afegir un customer rebut desde el body de la petició
    public void addCustomer(Customer customer) {
        customLogging.logInfo("CustomerRepository", "addCustomer", "Afegint customer: " + customer.getName());
        try {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            customer.setDataCreated(now);
            customer.setDataUpdated(now);

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
        } catch (Exception e) {
            customLogging.logError("CustomerRepository", "addCustomer", "Error afegint customer: " + customer.getName(), e);
            throw e;
        }
    }

    // Funció per obtenir un customer per ID
    public Customer findById(Long id) {
        customLogging.logInfo("CustomerRepository", "findById", "Consultant customer amb id: " + id);
        try {
            String sql = "SELECT * FROM customers WHERE id = ?";
            List<Customer> customers = jdbcTemplate.query(sql, new CustomerRowMapper(), id);
            if (customers.isEmpty()) {
                return null;
            } else {
                return customers.get(0);
            }
        } catch (Exception e) {
            customLogging.logError("CustomerRepository", "findById", "Error consultant customer amb id: " + id, e);
            throw e;
        }
    }

    // Funció per actualitzar un customer existent
    public void updateCustomer(Customer customer) {
        customLogging.logInfo("CustomerRepository", "updateCustomer", "Actualitzant customer amb id: " + customer.getId());
        try {
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
        } catch (Exception e) {
            customLogging.logError("CustomerRepository", "updateCustomer", "Error actualitzant customer amb id: " + customer.getId(), e);
            throw e;
        }
    }

    // Funció per actualitzar només l'age d'un customer
    public void updateCustomerAge(Customer customer) {
        customLogging.logInfo("CustomerRepository", "updateCustomerAge", "Actualitzant age del customer amb id: " + customer.getId());
        try {
            jdbcTemplate.update(
                    "UPDATE customers SET age = ?, dataUpdated = ? WHERE id = ?",
                    customer.getAge(),
                    customer.getDataUpdated(),
                    customer.getId()
            );
        } catch (Exception e) {
            customLogging.logError("CustomerRepository", "updateCustomerAge", "Error actualitzant age del customer amb id: " + customer.getId(), e);
            throw e;
        }
    }

    // Funció per eliminar un customer per ID
    public void deleteCustomer(Long id) {
        customLogging.logInfo("CustomerRepository", "deleteCustomer", "Eliminant customer amb id: " + id);
        try {
            jdbcTemplate.update("DELETE FROM customers WHERE id = ?", id);
        } catch (Exception e) {
            customLogging.logError("CustomerRepository", "deleteCustomer", "Error eliminant customer amb id: " + id, e);
            throw e;
        }
    }

    // Funció per actualitzar el camp image_path d'un customer
    public void updateCustomerImage(Long id, String imagePath) {
        customLogging.logInfo("CustomerRepository", "updateCustomerImage", "Actualitzant imatge del customer amb id: " + id);
        try {
            jdbcTemplate.update(
                    "UPDATE customers SET image_path = ?, dataUpdated = CURRENT_TIMESTAMP WHERE id = ?",
                    imagePath,
                    id
            );
        } catch (Exception e) {
            customLogging.logError("CustomerRepository", "updateCustomerImage", "Error actualitzant imatge del customer amb id: " + id, e);
            throw e;
        }
    }

}
