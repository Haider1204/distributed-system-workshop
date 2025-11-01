package co.edu.escuelaing.distributed.backend;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;

/**
 * Cliente mejorado para registrarse automáticamente en el Service Registry
 */
@Component
public class RegistryClient {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private ScheduledExecutorService scheduler;
    
    private String registryUrl;
    private String serviceId;
    private String serviceHost;
    private int servicePort;
    
    private volatile boolean isRegistered = false;
    private int failedHeartbeats = 0;
    private static final int MAX_FAILED_HEARTBEATS = 3;
    
    @PostConstruct
    public void init() {
        // Obtener configuración
        registryUrl = System.getProperty("REGISTRY_URL", "http://localhost:8080");
        serviceHost = System.getProperty("SERVICE_HOST", "localhost");
        servicePort = Integer.parseInt(System.getProperty("PORT", "8081"));
        serviceId = System.getProperty("NODE_NAME", "backend-" + servicePort);
        
        System.out.println("[REGISTRY-CLIENT] Initializing...");
        System.out.println("[REGISTRY-CLIENT] Registry URL: " + registryUrl);
        System.out.println("[REGISTRY-CLIENT] Service ID: " + serviceId);
        System.out.println("[REGISTRY-CLIENT] Service URL: http://" + serviceHost + ":" + servicePort);
        
        // Esperar un poco antes de registrar (dar tiempo a que Spring Boot inicie completamente)
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            registerService();
            startHeartbeat();
        }, 5, TimeUnit.SECONDS);
    }
    
    private void registerService() {
        int retries = 0;
        int maxRetries = 5;
        
        while (retries < maxRetries && !isRegistered) {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("id", serviceId);
                request.put("host", serviceHost);
                request.put("port", servicePort);
                
                String url = registryUrl + "/registry/register";
                
                System.out.println("[REGISTRY-CLIENT] Attempting registration (attempt " + (retries + 1) + "/" + maxRetries + ")...");
                
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
                
                if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                    isRegistered = true;
                    failedHeartbeats = 0;
                    System.out.println("[REGISTRY-CLIENT] ✅ Successfully registered with registry!");
                    return;
                } else {
                    System.err.println("[REGISTRY-CLIENT] ❌ Registration failed: " + response);
                }
                
            } catch (Exception e) {
                System.err.println("[REGISTRY-CLIENT] ❌ Registration attempt failed: " + e.getMessage());
            }
            
            retries++;
            if (retries < maxRetries) {
                try {
                    Thread.sleep(3000); // Esperar 3 segundos antes de reintentar
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        if (!isRegistered) {
            System.err.println("[REGISTRY-CLIENT] ⚠️ Failed to register after " + maxRetries + " attempts");
            System.err.println("[REGISTRY-CLIENT] ⚠️ Service will continue running but won't receive traffic from Load Balancer");
        }
    }
    
    private void startHeartbeat() {
        // Enviar heartbeat cada 15 segundos
        scheduler.scheduleAtFixedRate(() -> {
            if (isRegistered) {
                sendHeartbeat();
            } else {
                // Si no está registrado, intentar registrarse nuevamente
                System.out.println("[REGISTRY-CLIENT] Not registered, attempting re-registration...");
                registerService();
            }
        }, 15, 15, TimeUnit.SECONDS);
        
        System.out.println("[REGISTRY-CLIENT] Heartbeat started (every 15 seconds)");
    }
    
    private void sendHeartbeat() {
        try {
            String url = registryUrl + "/registry/heartbeat/" + serviceId;
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                failedHeartbeats = 0;
                System.out.println("[REGISTRY-CLIENT] ❤️ Heartbeat sent successfully");
            } else {
                handleFailedHeartbeat();
            }
            
        } catch (Exception e) {
            handleFailedHeartbeat();
            System.err.println("[REGISTRY-CLIENT] ❌ Heartbeat failed: " + e.getMessage());
        }
    }
    
    private void handleFailedHeartbeat() {
        failedHeartbeats++;
        System.err.println("[REGISTRY-CLIENT] ⚠️ Heartbeat failed (" + failedHeartbeats + "/" + MAX_FAILED_HEARTBEATS + ")");
        
        if (failedHeartbeats >= MAX_FAILED_HEARTBEATS) {
            System.err.println("[REGISTRY-CLIENT] ⚠️ Too many failed heartbeats, marking as not registered");
            isRegistered = false;
        }
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("[REGISTRY-CLIENT] Shutting down...");
        
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        
        // Desregistrar al cerrar
        if (isRegistered) {
            try {
                String url = registryUrl + "/registry/unregister/" + serviceId;
                restTemplate.delete(url);
                System.out.println("[REGISTRY-CLIENT] ✅ Unregistered from registry");
            } catch (Exception e) {
                System.err.println("[REGISTRY-CLIENT] ❌ Failed to unregister: " + e.getMessage());
            }
        }
    }
}