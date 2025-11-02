package com.ra2.mysql.controller;


import com.ra2.mysql.model.Customer;
import com.ra2.mysql.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jdbctemplate")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    // Endpoint per afegir 10 usuaris d'exemple
    @PostMapping("/add-sample-users")
    public ResponseEntity<String> addSampleUsers() {
        customerRepository.addSampleUsers();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("S'han afegit correctament 10 usuaris de prova");
    }

    // Endpoint GET per obtenir tots els usuaris
    @GetMapping("/findAllCustomers")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();    // Retorna una llista amb tots els customers
    }

    // Endpoint POST per crear un nou usuari a partir d'un JSON
    @PostMapping("/api/customer")
    public ResponseEntity<String> createCustomer(@RequestBody Customer customer) {
        customerRepository.addCustomer(customer); // inserta un customer rebut
        return ResponseEntity.status(HttpStatus.CREATED).body("Customer afegit correctament");
    }

    // Endpoint GET per obtenir tots els customers
    @GetMapping("/api/customer")
    public ResponseEntity<List<Customer>> getAllCustomersApi() {
        List<Customer> customers = customerRepository.findAll(); // Obtenim tots els customers

        if (customers.isEmpty()) {
            // Si no hi ha usuaris, retornem null
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } else {
            // Retornem la llista de customers
            return ResponseEntity.status(HttpStatus.OK).body(customers);
        }
    }

    // Endpoint GET per obtenir un customer segons l'ID
    @GetMapping("/api/customer/{customer_id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long customer_id) {
        // Busquem el customer per ID
        Customer customer = customerRepository.findById(customer_id);

        if (customer == null) {
            // Si no troba el customer, retornem null
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } else {
            // Si troba el customer, retornem l'objecte
            return ResponseEntity.status(HttpStatus.OK).body(customer);
        }
    }

    // Endpoint PUT per actualitzar completament un customer
    @PutMapping("/api/customer/{customer_id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long customer_id,
            @RequestBody Customer customerDetails) {

        // Busquem el customer existent
        Customer customerExistent = customerRepository.findById(customer_id);

        if (customerExistent == null) {
            // Si no existeix, retornem null
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }

        // Actualitzem tots els camps amb la informació rebuda
        customerExistent.setName(customerDetails.getName());
        customerExistent.setDescription(customerDetails.getDescription());
        customerExistent.setAge(customerDetails.getAge());
        customerExistent.setCourse(customerDetails.getCourse());
        customerExistent.setPassword(customerDetails.getPassword());

        // Actualitzem la dataUpdated amb la data actual
        customerExistent.setDataUpdated(new java.sql.Timestamp(System.currentTimeMillis()));

        // Guardem els canvis a la base de dades
        customerRepository.updateCustomer(customerExistent);

        // Retornem el customer actualitzat
        return ResponseEntity.status(HttpStatus.OK).body(customerExistent);
    }

    // Endpoint PATCH per actualitzar només l'age d'un customer
    @PatchMapping("/api/customer/{customer_id}/age")
    public ResponseEntity<Customer> updateCustomerAge(
            @PathVariable Long customer_id,
            @RequestParam int age) {

        // Busquem el customer existent
        Customer existingCustomer = customerRepository.findById(customer_id);

        if (existingCustomer == null) {
            // Si no existeix, retornem null
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }

        // Actualitzem només el camp age
        existingCustomer.setAge(age);

        // Actualitzem la dataUpdated amb la data actual
        existingCustomer.setDataUpdated(new java.sql.Timestamp(System.currentTimeMillis()));

        // Guardem els canvis a la base de dades
        customerRepository.updateCustomerAge(existingCustomer);

        // Retornem el customer actualitzat
        return ResponseEntity.status(HttpStatus.OK).body(existingCustomer);
    }

    // Endpoint DELETE per esborrar un customer
    @DeleteMapping("/api/customer/{customer_id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable Long customer_id) {

        // Busquem el customer per ID
        Customer existingCustomer = customerRepository.findById(customer_id);

        if (existingCustomer == null) {
            // Si no existeix, retornem missatge indicant que no s'ha trobat
            return ResponseEntity.status(HttpStatus.OK).body("Customer amb ID " + customer_id + " no existeix");
        }

        // Si existeix, eliminem el customer
        customerRepository.deleteCustomer(customer_id);

        // Retornem missatge confirmant l'eliminació
        return ResponseEntity.status(HttpStatus.OK).body("Customer amb ID " + customer_id + " eliminat correctament");
    }
}
