# spring_jdbc_mysql

# Pràctica: Gestió de fitxers (JSON, CSV i Imatges) amb Spring Boot

Aquest projecte implementa tres funcionalitats principals relacionades amb la càrrega i processament de fitxers al servidor mitjançant Spring Boot i JDBC.

---

## Funcionalitats Implementades

### 1. **Pujada d’una imatge associada a un Customer**
Endpoint que permet pujar una imatge, guardar-la en una carpeta pública i emmagatzemar-ne la ruta a la base de dades.


### 2. **Pujada d’un fitxer CSV i inserció massiva**
Endpoint que permet carregar un arxiu CSV, llegir-lo línia per línia i inserir cada registre a la base de dades.

### 3. **Pujada i processament d’un fitxer JSON**
Endpoint que permet pujar un fitxer JSON, llegir-ne el contingut, extreure la llista d’usuaris i afegir-los a la base de dades.

---

## Vídeo demostració
https://drive.google.com/file/d/1zgOIEHniIGFYCMGo8flrROXXms8cOTLq/view?usp=sharing

---

### Canvis realitzats
- S'ha afegit l'`@Autowired` que faltava a `CustomerService` perquè funcionés correctament.
- S'ha corregit la ruta on es guarden els fitxers per tal que es guardin a la carpeta correcta.