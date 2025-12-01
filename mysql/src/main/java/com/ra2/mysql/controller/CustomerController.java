package com.ra2.mysql.controller;


import com.ra2.mysql.model.Customer;
import com.ra2.mysql.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // GET per obtenir tots els usuaris
    @GetMapping("/findAllCustomers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.status(HttpStatus.OK).body(customers);
    }

    // GET per obtenir un customer per ID
    @GetMapping("/customer/{customer_id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long customer_id) {
        try {
            Customer customer = customerService.getCustomerById(customer_id);
            return ResponseEntity.status(HttpStatus.OK).body(customer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // POST per crear un nou customer
    @PostMapping("/customer")
    public ResponseEntity<String> createCustomer(@RequestBody Customer customer) {
        try {
            customerService.addCustomer(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body("Customer afegit correctament");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // PUT per actualitzar completament un customer
    @PutMapping("/customer/{customer_id}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable Long customer_id,
            @RequestBody Customer customerDetails) {

        try {
            Customer updated = customerService.updateCustomer(customer_id, customerDetails);
            return ResponseEntity.status(HttpStatus.OK).body(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // PATCH per qctualitzar només l'age
    @PatchMapping("/customer/{customer_id}/age")
    public ResponseEntity<?> updateCustomerAge(
            @PathVariable Long customer_id,
            @RequestParam int age) {

        try {
            Customer updated = customerService.updateCustomerAge(customer_id, age);
            return ResponseEntity.status(HttpStatus.OK).body(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // DELETE per esborrar un customer
    @DeleteMapping("/customer/{customer_id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable Long customer_id) {
        String msg = customerService.deleteCustomer(customer_id);
        return ResponseEntity.status(HttpStatus.OK).body(msg);
    }

    // POST per afegir la imatge d'un customer
    @PostMapping("/users/{user_id}/image")
    public ResponseEntity<String> uploadCustomerImage(
            @PathVariable Long user_id,
            @RequestParam MultipartFile imageFile) {

        // Cridem al Service i retornem el resultat directament
        String imageUrl = customerService.saveCustomerImage(user_id, imageFile);
        return ResponseEntity.status(HttpStatus.OK).body("Imatge pujada correctament: " + imageUrl);
    }

    // POST per pujar un fitxer CSV i carregar dades massives
    @PostMapping("/users/upload-csv")
    public ResponseEntity<String> uploadCsv(@RequestParam MultipartFile csvFile ) throws IOException {
        // Crida al service per processar el CSV
        int totalAdded = customerService.uploadCsv(csvFile);

        return ResponseEntity.status(HttpStatus.OK).body("S'han afegit " + totalAdded + " customers correctament.");
    }

    // POST per pujar un JSON amb usuaris
    @PostMapping("/users/upload-json")
    public ResponseEntity<String> uploadJson(@RequestParam MultipartFile jsonFile) throws IOException {
        // Cridar al service per processar el JSON
        int totalAdded = customerService.uploadJson(jsonFile);

        return ResponseEntity.status(HttpStatus.OK).body("S'han afegit " + totalAdded + " usuaris correctament");
    }
}
