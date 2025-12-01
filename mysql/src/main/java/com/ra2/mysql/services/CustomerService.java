/**
 * CustomerService
 *
 * Conté la lògica de negoci relacionada amb els clients (Customers),
 * incloent validacions i gestió de timestamps.
 *
 * Responsable de:
 * - Afegir nous clients amb validacions (nom, edat, contrasenya, descripció)
 * - Obtenir clients (tots o per ID)
 * - Actualitzar clients (complet o parcialment)
 * - Eliminar clients
 *
 * Aquesta capa separa la lògica de negoci del Repository i del Controller.
 */
package com.ra2.mysql.services;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ra2.mysql.model.Customer;
import com.ra2.mysql.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {

    @Autowired
    ObjectMapper mapper;
    @Autowired
    private CustomerRepository customerRepository;

    public void addCustomer(Customer customer) {
        // totes les validacions
        validateCustomer(customer);

        // Afegir timestamps
        Timestamp now = new Timestamp(System.currentTimeMillis());
        customer.setDataCreated(now);
        customer.setDataUpdated(now);

        // GUardem a la base de dades
        customerRepository.addCustomer(customer);
    }

    // OBtenim tots els customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id);
        if (customer == null) {
            throw new IllegalArgumentException("No s'ha trobat cap customer amb ID " + id);
        }
        return customer;
    }

    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer existingCustomer = customerRepository.findById(id);

        if (existingCustomer == null) {
            throw new IllegalArgumentException("No existeix el customer amb ID " + id);
        }

        // Validacions
        if (customerDetails.getName() != null) validateName(customerDetails.getName());
        if (customerDetails.getAge() != 0) validateAge(customerDetails.getAge());
        if (customerDetails.getPassword() != null) validatePassword(customerDetails.getPassword());
        if (customerDetails.getDescription() != null) validateDescription(customerDetails.getDescription());
        if (customerDetails.getCourse() != null) validateCourse(customerDetails.getCourse());

        // Actualitzem els camps
        existingCustomer.setName(customerDetails.getName());
        existingCustomer.setDescription(customerDetails.getDescription());
        existingCustomer.setAge(customerDetails.getAge());
        existingCustomer.setCourse(customerDetails.getCourse());
        existingCustomer.setPassword(customerDetails.getPassword());

        // Actualitzem els timestamp
        existingCustomer.setDataUpdated(new Timestamp(System.currentTimeMillis()));

        // Guardem a la base de dades
        customerRepository.updateCustomer(existingCustomer);

        return existingCustomer;
    }

    public Customer updateCustomerAge(Long id, int age) {
        Customer existingCustomer = customerRepository.findById(id);

        if (existingCustomer == null) {
            throw new IllegalArgumentException("No s'ha trobat cap customer amb ID " + id);
        }

        validateAge(age);

        // Actualitzem solament el age
        existingCustomer.setAge(age);
        existingCustomer.setDataUpdated(new Timestamp(System.currentTimeMillis()));

        customerRepository.updateCustomerAge(existingCustomer);
        return existingCustomer;
    }

    public String deleteCustomer(Long id) {
        Customer existingCustomer = customerRepository.findById(id);

        if (existingCustomer == null) {
            return "Customer amb ID " + id + " no existeix";
        }

        customerRepository.deleteCustomer(id);
        return "Customer amb ID " + id + " eliminat correctament";
    }

    // Mètode per guardar la imatge d'un customer i retornar la seva URL
    public String saveCustomerImage(Long customerId, MultipartFile imageFile)  {
        // 1. Comprovar si existeix el customer
        Customer existingCustomer = customerRepository.findById(customerId);
        if (existingCustomer == null) {
            throw new IllegalArgumentException("Customer amb ID " + customerId + " no existeix");
        }

        // 2. Crear carpeta src/main/resources/public/images si no existeix
        Path uploadDir = Paths.get("mysql/src/main/resources/public/images");
        try {
            Files.createDirectories(uploadDir); // Crea el directori si no existeix
        } catch (IOException e) {
            throw new RuntimeException("Error al crear la carpeta d'imatges: " + e.getMessage());
        }

        try {
            // 3. Crear un nom únic per la imatge
            String originalFilename = imageFile.getOriginalFilename(); // nom original
            String uniqueFilename = customerId + "_" + System.currentTimeMillis() + "_" + originalFilename;

            // 4. Ruta completa on guardar la imatge
            Path filePath = uploadDir.resolve(uniqueFilename);

            // 5. Guardar la imatge al disc
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 6. Guardar ruta relativa per a la BD
            String relativePath = "images/" + uniqueFilename;

            // 7. Actualitzar la BD amb la ruta de la imatge
            customerRepository.updateCustomerImage(customerId, relativePath);

            // Retornar la URL relativa de la imatge
            return relativePath;

        } catch (IOException e) {
            // Captura errors d'entrada/sortida
            throw new RuntimeException("Error al guardar la imatge: " + e.getMessage());
        }
    }

    // Funció per processar un fitxer CSV i afegir customers a la base de dades
    public int uploadCsv(MultipartFile csvFile) throws IOException {
        int totalAdded = 0; // Comptador de registres afegits

        // 1. Llegir el CSV directament des del MultipartFile amb InputStreamReader
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                // 2. Saltar capçalera
                if (lineNumber == 1) continue;

                // 3. Separar camps per comes
                String[] fields = line.split(",");

                // 4. Crear un customer amb els camps del CSV
                Customer customer = new Customer();
                customer.setName(fields[0].trim());
                customer.setDescription(fields[1].trim());
                customer.setAge(Integer.parseInt(fields[2].trim()));
                customer.setCourse(fields[3].trim());
                customer.setPassword(fields[4].trim());

                // 5. Afegir customer a la base de dades
                customerRepository.addCustomer(customer);
                totalAdded++;
            }
        }

        // 6. Crear carpeta csv_processed si no existeix
        Path processedDir = Paths.get("mysql/src/main/resources/public/csv_processed");
        Files.createDirectories(processedDir);

        // 7. Guardar el fitxer CSV original a la carpeta processada
        Path targetPath = processedDir.resolve(csvFile.getOriginalFilename());
        Files.copy(csvFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 8. Retornar el total de registres afegits
        return totalAdded;
    }

    // Funció per processar un fitxer JSON i afegir usuaris a la base de dades
    public int uploadJson(MultipartFile jsonFile) throws IOException {
        int totalAdded = 0; // Comptador de registres afegits

        // 1. Llegir el JSON directament des de l'InputStream del MultipartFile
        JsonNode arrel = mapper.readTree(jsonFile.getInputStream());

        // 2. Accedir al node "data"
        JsonNode dataNode = arrel.path("data");

        // 3. Accedir al node "users" que és un array
        JsonNode usersNode = dataNode.path("customers");

        // 4. Iterar sobre cada usuari del JSON
        for (JsonNode userNode : usersNode) {
            // 4a. Crear un Customer amb els camps del JSON
            Customer customer = new Customer();
            customer.setName(userNode.path("name").asText());
            customer.setDescription(userNode.path("description").asText());
            customer.setAge(userNode.path("age").asInt());
            customer.setCourse(userNode.path("course").asText());
            customer.setPassword(userNode.path("password").asText());

            // 4b. Afegir el Customer a la base de dades
            customerRepository.addCustomer(customer);
            totalAdded++;
        }

        // 5. Crear carpeta json_processed si no existeix
        Path processedDir = Paths.get("mysql/src/main/resources/public/json_processed");
        Files.createDirectories(processedDir);

        // 6. Guardar el fitxer JSON a la carpeta processada
        Path targetPath = processedDir.resolve(jsonFile.getOriginalFilename());
        Files.copy(jsonFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return totalAdded; // Retornar el total de registres afegits
    }

    // MÉTODES DE VALIDACIÓ

    private void validateCustomer(Customer customer) {
        validateName(customer.getName());
        validateAge(customer.getAge());
        validatePassword(customer.getPassword());
        validateDescription(customer.getDescription());
        validateCourse(customer.getCourse());
    }

    private void validateName(String name) {
        if (name == null || name.length() < 3) {
            throw new IllegalArgumentException("El nom ha de tenir com a mínim 3 caràcters");
        }
    }

    private void validateAge(int age) {
        if (age < 16 || age > 100) {
            throw new IllegalArgumentException("L'edat ha de ser entre 16 i 100 anys");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contrasenya ha de tenir com a mínim 8 caràcters");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripció no pot estar buida");
        }
    }

    private void validateCourse(String course) {
        if (course == null || course.trim().isEmpty()) {
            throw new IllegalArgumentException("El curs no pot estar buit");
        }

        // Cursos vàlids
        List<String> validCourses = List.of("DAM", "DAW", "ASIX");

        if (!validCourses.contains(course.toUpperCase())) {
            throw new IllegalArgumentException("El curs ha de ser DAM, DAW o ASIX");
        }
    }

}
