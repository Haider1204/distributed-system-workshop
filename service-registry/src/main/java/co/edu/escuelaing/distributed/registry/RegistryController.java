package co.edu.escuelaing.distributed.registry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller para el Service Registry
 */
@RestController
@RequestMapping("/registry")
@CrossOrigin(origins = "*")
public class RegistryController {
    
    @Autowired
    private RegistryService registryService;
    
    /**
     * Registrar un nuevo servicio backend
     * POST /registry/register
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> request) {
        try {
            String id = (String) request.get("id");
            String host = (String) request.get("host");
            Integer port = (Integer) request.get("port");
            
            if (id == null || host == null || port == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Missing required fields"));
            }
            
            ServiceInstance instance = registryService.register(id, host, port);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Service registered successfully");
            response.put("instance", instance);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Desregistrar un servicio
     * DELETE /registry/unregister/{id}
     */
    @DeleteMapping("/unregister/{id}")
    public ResponseEntity<Map<String, Object>> unregister(@PathVariable String id) {
        boolean removed = registryService.unregister(id);
        
        if (removed) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Service unregistered"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "Service not found"));
        }
    }
    
    /**
     * Heartbeat de un servicio
     * POST /registry/heartbeat/{id}
     */
    @PostMapping("/heartbeat/{id}")
    public ResponseEntity<Map<String, Object>> heartbeat(@PathVariable String id) {
        boolean updated = registryService.heartbeat(id);
        
        if (updated) {
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "Service not found"));
        }
    }
    
    /**
     * Obtener todas las instancias registradas
     * GET /registry/instances
     */
    @GetMapping("/instances")
    public ResponseEntity<Map<String, Object>> getAllInstances() {
        List<ServiceInstance> instances = registryService.getAllInstances();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", instances.size());
        response.put("instances", instances);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtener solo instancias activas
     * GET /registry/instances/active
     */
    @GetMapping("/instances/active")
    public ResponseEntity<Map<String, Object>> getActiveInstances() {
        List<ServiceInstance> instances = registryService.getActiveInstances();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", instances.size());
        response.put("instances", instances);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtener una instancia específica
     * GET /registry/instances/{id}
     */
    @GetMapping("/instances/{id}")
    public ResponseEntity<Map<String, Object>> getInstance(@PathVariable String id) {
        ServiceInstance instance = registryService.getInstance(id);
        
        if (instance != null) {
            return ResponseEntity.ok(Map.of("success", true, "instance", instance));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "Instance not found"));
        }
    }
    
    /**
     * Limpiar instancias inactivas
     * POST /registry/cleanup
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanup() {
        int removed = registryService.cleanupInactiveInstances();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Cleanup completed",
            "removed", removed
        ));
    }
    
    /**
     * Health check
     * GET /registry/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        int activeCount = registryService.getActiveInstances().size();
        int totalCount = registryService.getAllInstances().size();
        
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "activeServices", activeCount,
            "totalServices", totalCount
        ));
    }
}