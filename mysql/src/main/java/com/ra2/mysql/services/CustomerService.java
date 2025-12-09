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
import com.ra2.mysql.logging.CustomLogging;
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

    @Autowired
    private CustomLogging customLogging;

    public void addCustomer(Customer customer) {
        customLogging.logInfo("CustomerService", "addCustomer", "Creant un customer");

        try {
            // totes les validacions
            validateCustomer(customer);
            // Afegir timestamps
            Timestamp now = new Timestamp(System.currentTimeMillis());
            customer.setDataCreated(now);
            customer.setDataUpdated(now);
            // GUardem a la base de dades
            customerRepository.addCustomer(customer);

            customLogging.logInfo("CustomerService", "addCustomer", "Customer creat correctament");
        } catch (Exception e) {
            customLogging.logError("CustomerService", "addCustomer",
                    "L'estudiant amb nom: " + customer.getName() + " no s'ha creat correctament. Missatge d'error: " + e.getMessage(), e);
            throw e;
        }
    }

    public List<Customer> getAllCustomers() {
        customLogging.logInfo("CustomerService", "getAllCustomers", "Consulta tots els customers");
        try {
            return customerRepository.findAll();
        } catch (Exception e) {
            customLogging.logError("CustomerService", "getAllCustomers", "Error obtenint tots els customers", e);
            throw e;
        }
    }

    public Customer getCustomerById(Long id) {
        customLogging.logInfo("CustomerService", "getCustomerById", "Consultant customer amb id: " + id);
        try {
            return customerRepository.findById(id);
        } catch (Exception e) {
            customLogging.logError("CustomerService", "getCustomerById", "Error consultant customer amb id: " + id, e);
            throw e;
        }
    }

    public Customer updateCustomer(Long id, Customer customerDetails) {
        customLogging.logInfo("CustomerService", "updateCustomer", "Modificant customer amb id: " + id);
        try {
            Customer existingCustomer = customerRepository.findById(id);

            // Si no existeix, llencem excepció directament
            if (existingCustomer == null) {
                throw new IllegalArgumentException("El customer amb id: " + id + " no existeix");
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
            existingCustomer.setDataUpdated(new Timestamp(System.currentTimeMillis()));

            customerRepository.updateCustomer(existingCustomer);

            return existingCustomer;
        } catch (Exception e) {
            customLogging.logError("CustomerService", "updateCustomer", "Error actualitzant customer amb id: " + id, e);
            throw e;
        }
    }

    public Customer updateCustomerAge(Long id, int age) {
        customLogging.logInfo("CustomerService", "updateCustomerAge", "Modificant l'edat del customer amb id: " + id);
        try {
            Customer existingCustomer = customerRepository.findById(id);

            if (existingCustomer == null) {
                throw new IllegalArgumentException("El customer amb id: " + id + " no existeix");
            }

            validateAge(age);

            // Actualitzem només l'edat
            existingCustomer.setAge(age);
            existingCustomer.setDataUpdated(new Timestamp(System.currentTimeMillis()));

            customerRepository.updateCustomerAge(existingCustomer);

            return existingCustomer;
        } catch (Exception e) {
            customLogging.logError("CustomerService", "updateCustomerAge", "Error modificant l'edat del customer amb id: " + id, e);
            throw e;
        }
    }

    public String deleteCustomer(Long id) {
        customLogging.logInfo("CustomerService", "deleteCustomer", "Borrant el customer amb id: " + id);
        try {
            Customer existingCustomer = customerRepository.findById(id);

            if (existingCustomer == null) {
                throw new IllegalArgumentException("El customer amb id: " + id + " no existeix");
            }

            customerRepository.deleteCustomer(id);

            return "Customer amb ID " + id + " eliminat correctament";
        } catch (Exception e) {
            customLogging.logError("CustomerService", "deleteCustomer", "Error borrant el customer amb id: " + id, e);
            throw e;
        }
    }

    public String saveCustomerImage(Long customerId, MultipartFile imageFile) {
        customLogging.logInfo("CustomerService", "saveCustomerImage", "Afegint la imatge " + imageFile.getOriginalFilename() + " per al customer amb id: " + customerId);
        try {
            // 1. Comprovar si existeix el customer
            Customer existingCustomer = customerRepository.findById(customerId);
            if (existingCustomer == null) {
                throw new IllegalArgumentException("Customer amb ID " + customerId + " no existeix");
            }

            // 2. Crear carpeta src/main/resources/public/images si no existeix
            Path uploadDir = Paths.get("mysql/src/main/resources/public/images");
            Files.createDirectories(uploadDir);

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

            customLogging.logInfo("CustomerService", "saveCustomerImage", "La imatge s'ha guardat correctament. El path és: " + relativePath);
            return relativePath;

        } catch (Exception e) {
            customLogging.logError("CustomerService", "saveCustomerImage", "Error afegint la imatge " + imageFile.getOriginalFilename() + " per al customer amb id: " + customerId, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public int uploadCsv(MultipartFile csvFile) {
        customLogging.logInfo("CustomerService", "uploadCsv", "Carregant la informació del fitxer " + csvFile.getOriginalFilename());
        int totalAdded = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                // Saltar capçalera
                if (lineNumber == 1) continue;

                try {
                    // Separar camps per comes
                    String[] fields = line.split(",");

                    // Crear un customer amb els camps del CSV
                    Customer customer = new Customer();
                    customer.setName(fields[0].trim());
                    customer.setDescription(fields[1].trim());
                    customer.setAge(Integer.parseInt(fields[2].trim()));
                    customer.setCourse(fields[3].trim());
                    customer.setPassword(fields[4].trim());

                    // Afegir customer a la base de dades
                    customerRepository.addCustomer(customer);
                    totalAdded++;

                } catch (Exception e) {
                    customLogging.logError(
                            "CustomerService",
                            "uploadCsv",
                            "Error en la línia " + lineNumber + " del fitxer. Missatge d'error: " + e.getMessage(),
                            e
                    );
                }
            }

            // Crear carpeta csv_processed si no existeix
            Path processedDir = Paths.get("mysql/src/main/resources/public/csv_processed");
            Files.createDirectories(processedDir);

            // Guardar el fitxer CSV original a la carpeta processada
            Path targetPath = processedDir.resolve(csvFile.getOriginalFilename());
            Files.copy(csvFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            customLogging.logInfo(
                    "CustomerService",
                    "uploadCsv",
                    "S'han guardat correctament " + totalAdded + " registres"
            );

        } catch (Exception e) {
            customLogging.logError(
                    "CustomerService",
                    "uploadCsv",
                    "Error processant el fitxer " + csvFile.getOriginalFilename(),
                    e
            );
            throw new RuntimeException(e.getMessage(), e);
        }

        return totalAdded;
    }

    public int uploadJson(MultipartFile jsonFile) {
        customLogging.logInfo("CustomerService", "uploadJson", "Carregant la informació del fitxer " + jsonFile.getOriginalFilename());
        int totalAdded = 0;

        try {
            // Llegir el JSON directament des de l'InputStream del MultipartFile
            JsonNode arrel = mapper.readTree(jsonFile.getInputStream());

            // Accedir al node "data"
            JsonNode dataNode = arrel.path("data");

            // Accedir al node "customers" que és un array
            JsonNode usersNode = dataNode.path("customers");

            int index = 0;
            for (JsonNode userNode : usersNode) {
                index++;
                try {
                    // Crear un Customer amb els camps del JSON
                    Customer customer = new Customer();
                    customer.setName(userNode.path("name").asText());
                    customer.setDescription(userNode.path("description").asText());
                    customer.setAge(userNode.path("age").asInt());
                    customer.setCourse(userNode.path("course").asText());
                    customer.setPassword(userNode.path("password").asText());

                    // Afegir el Customer a la base de dades
                    customerRepository.addCustomer(customer);
                    totalAdded++;
                } catch (Exception e) {
                    customLogging.logError(
                            "CustomerService",
                            "uploadJson",
                            "Error en l'usuari número " + index + " del fitxer. Missatge d'error: " + e.getMessage(),
                            e
                    );
                }
            }

            // Crear carpeta json_processed si no existeix
            Path processedDir = Paths.get("mysql/src/main/resources/public/json_processed");
            Files.createDirectories(processedDir);

            // Guardar el fitxer JSON a la carpeta processada
            Path targetPath = processedDir.resolve(jsonFile.getOriginalFilename());
            Files.copy(jsonFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            customLogging.logInfo(
                    "CustomerService",
                    "uploadJson",
                    "S'han guardat correctament " + totalAdded + " registres"
            );

        } catch (Exception e) {
            customLogging.logError(
                    "CustomerService",
                    "uploadJson",
                    "Error processant el fitxer " + jsonFile.getOriginalFilename(),
                    e
            );
            throw new RuntimeException(e.getMessage(), e);
        }

        return totalAdded;
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
