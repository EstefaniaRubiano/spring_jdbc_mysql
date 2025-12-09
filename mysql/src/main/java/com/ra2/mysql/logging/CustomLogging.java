package com.ra2.mysql.logging;

import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe per escriure logs en un fitxer per dia dins la carpeta "logs" a l'arrel del projecte.
 * Format línia: [YYYY-MM-DD HH:mm:ss] LEVEL - CLASS - METHOD - DESCRIPTION
 */
@Component
public class CustomLogging {

    // Crear fitxer application.log
    private static final String LOG_FILE = "mysql/src/main/resources/logs/application.log";
    // Crear registre de data per la creació de log
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    // Escriu un missatge d'ERROR amb informació de classe/metode i l'excepció (si n'hi ha).
    public void logError(String className, String methodName, String errorMsg, Exception exception) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[ERROR] %s - CLass: %s - Method: %s - Message: %s", timestamp, className, methodName, errorMsg);

        if (exception != null) {
            logEntry += " - Exception: " + exception.getMessage();
        }

        writeToFile(logEntry);

        System.out.println(logEntry);
    }

    // Escriu un missatge d'INFO amb informació de classe/metode.
    public void logInfo(String className, String methodName, String infoMsg) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[INFO] %s - Class: %s - Method: %s - Message: %s", timestamp, className, methodName, infoMsg);

        writeToFile(logEntry);

        System.out.println(logEntry);
    }

    // Mètode privat que s'encarrega d'obrir/crear el fitxer diari i afegir la línia
    private void writeToFile(String message) {
        Path logPath = Paths.get(LOG_FILE);
        try {
            // Crear carpeta logs si no existeix
            Files.createDirectories(logPath.getParent());
            // Obrir/crear i afegir (append)
            try (BufferedWriter bw = Files.newBufferedWriter(logPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                bw.write(message);
                bw.newLine();
            }
        } catch (IOException e) {
            // Si falla l'escriptura, només ho deixem per consola perquè no peti tota l'app
            System.err.println("ERROR escrivint al fitxer de log: " + e.getMessage());
        }
    }
}
