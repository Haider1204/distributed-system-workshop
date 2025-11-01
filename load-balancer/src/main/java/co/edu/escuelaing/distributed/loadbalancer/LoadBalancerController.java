package co.edu.escuelaing.distributed.loadbalancer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller del Load Balancer
 * Recibe peticiones del cliente y las distribuye entre backends
 */
@RestController
@CrossOrigin(origins = "*")
public class LoadBalancerController {
    
    @Autowired
    private RoundRobinLoadBalancer loadBalancer;
    
    /**
     * Endpoint para registrar nombres (redirige a backend)
     * POST /api/register
     */
    @PostMapping("/api/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        System.out.println("[LB-CONTROLLER] Received register request: " + request);
        Map<String, Object> response = loadBalancer.forwardPostRequest("/api/register", request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para obtener nombres (redirige a backend)
     * GET /api/names
     */
    @GetMapping("/api/names")
    public ResponseEntity<Map<String, Object>> getNames() {
        System.out.println("[LB-CONTROLLER] Received get names request");
        Map<String, Object> response = loadBalancer.forwardGetRequest("/api/names");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint de estadísticas del load balancer
     * GET /api/stats
     */
    @GetMapping("/api/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = loadBalancer.getStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Health check
     * GET /api/health
     */
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Load Balancer"
        ));
    }
}