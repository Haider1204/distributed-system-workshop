package co.edu.escuelaing.distributed.loadbalancer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

/**
 * Aplicación principal del Load Balancer
 */
@SpringBootApplication
public class LoadBalancerApplication {
    
    public static void main(String[] args) {
        String port = System.getProperty("PORT", System.getenv("PORT"));
        if (port == null) {
            port = "8090"; // Puerto por defecto para el load balancer
        }
        
        System.out.println("========================================");
        System.out.println("Starting Load Balancer");
        System.out.println("Port: " + port);
        System.out.println("Registry: " + System.getProperty("REGISTRY_URL", "http://localhost:8080"));
        System.out.println("Strategy: Round-Robin");
        System.out.println("========================================");
        
        SpringApplication app = new SpringApplication(LoadBalancerApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", port));
        app.run(args);
    }
}