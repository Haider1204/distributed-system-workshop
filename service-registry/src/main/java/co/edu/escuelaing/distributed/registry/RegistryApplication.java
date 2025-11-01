package co.edu.escuelaing.distributed.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

/**
 * Aplicación principal del Service Registry
 */
@SpringBootApplication
public class RegistryApplication {
    
    public static void main(String[] args) {
        String port = System.getProperty("PORT", System.getenv("PORT"));
        if (port == null) {
            port = "8080"; // Puerto por defecto para el registry
        }
        
        System.out.println("========================================");
        System.out.println("Starting Service Registry");
        System.out.println("Port: " + port);
        System.out.println("========================================");
        
        SpringApplication app = new SpringApplication(RegistryApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", port));
        app.run(args);
    }
}