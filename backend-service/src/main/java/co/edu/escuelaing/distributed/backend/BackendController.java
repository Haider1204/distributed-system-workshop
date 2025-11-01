package co.edu.escuelaing.distributed.backend;

import co.edu.escuelaing.distributed.datastore.ReplicatedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller para operaciones de registro de nombres
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Permitir peticiones desde cualquier origen (para el frontend)
public class BackendController {
    
    @Autowired
    private ReplicatedHashMap dataStore;
    
    /**
     * Endpoint para registrar un nombre
     * POST /api/register
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerName(@RequestBody RegisterNameRequest request) {
        try {
            String name = request.getName();
            
            if (name == null || name.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Name cannot be empty");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Registrar con timestamp actual
            long timestamp = System.currentTimeMillis();
            dataStore.put(name.trim(), timestamp);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Name registered successfully");
            response.put("name", name.trim());
            response.put("timestamp", timestamp);
            
            System.out.println("[BACKEND] Registered: " + name + " at " + timestamp);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error registering name: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Endpoint para obtener todos los nombres registrados
     * GET /api/names
     */
    @GetMapping("/names")
    public ResponseEntity<Map<String, Object>> getAllNames() {
        try {
            Map<String, Long> allData = dataStore.getAll();
            
            // Convertir a lista de NameEntry y ordenar por timestamp (más reciente primero)
            List<NameEntry> entries = allData.entrySet().stream()
                .map(e -> new NameEntry(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", entries.size());
            response.put("entries", entries);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error retrieving names: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Endpoint para obtener información del nodo
     * GET /api/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getNodeInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("nodeName", System.getProperty("NODE_NAME", "unknown"));
        info.put("port", System.getProperty("server.port", "unknown"));
        info.put("dataStoreSize", dataStore.size());
        info.put("status", "active");
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * Health check endpoint
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("node", System.getProperty("NODE_NAME", "unknown"));
        return ResponseEntity.ok(health);
    }
}