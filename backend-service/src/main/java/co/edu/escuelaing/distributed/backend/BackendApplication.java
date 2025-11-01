package co.edu.escuelaing.distributed.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

/**
 * Aplicación principal del Backend Service
 */
@SpringBootApplication
public class BackendApplication {
    
    public static void main(String[] args) {
        // Obtener puerto y nombre de nodo de variables de entorno o argumentos
        String port = System.getProperty("PORT", System.getenv("PORT"));
        if (port == null) {
            port = "8081"; // Puerto por defecto
        }
        
        String nodeName = System.getProperty("NODE_NAME", System.getenv("NODE_NAME"));
        if (nodeName == null) {
            nodeName = "backend-" + port;
        }
        
        System.out.println("========================================");
        System.out.println("Starting Backend Service");
        System.out.println("Node Name: " + nodeName);
        System.out.println("Port: " + port);
        System.out.println("========================================");
        
        // Configurar propiedades del sistema
        System.setProperty("NODE_NAME", nodeName);
        
        // Crear aplicación Spring
        SpringApplication app = new SpringApplication(BackendApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", port));
        app.run(args);
    }
}