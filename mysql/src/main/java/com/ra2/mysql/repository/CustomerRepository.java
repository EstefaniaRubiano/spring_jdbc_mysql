package com.ra2.mysql.repository;

import com.ra2.mysql.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CustomerRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
            customer.setDataCreated(rs.getTimestamp("dataCreated"));
            customer.setDataUpdated(rs.getTimestamp("dataUpdated"));

            return customer;
        }
    }

    public void initDB() {
        jdbcTemplate.update("INSERT INTO customers () VALUES (?)", new Object[]{});
    }

    public List<Customer> findAll() {
        String sql = "SELECT id, name, description, age, course, password, data_created, data_updated FROM customers";
        return jdbcTemplate.query(sql, new CustomerRowMapper());
    }
}
