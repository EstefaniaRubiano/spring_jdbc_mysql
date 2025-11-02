package com.ra2.mysql.controller;


import com.ra2.mysql.model.Customer;
import com.ra2.mysql.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/jdbctemplate")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/initdataDB")
    public String initDataDB() {
        customerRepository.initDB();
        return "Taula emplenada correctament";
    }

    @GetMapping("findAllCustomers")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
}
