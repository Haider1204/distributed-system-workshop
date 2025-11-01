package co.edu.escuelaing.distributed.loadbalancer;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio de Load Balancer con estrategia Round-Robin
 */
@Service
public class RoundRobinLoadBalancer {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    
    private String registryUrl;
    
    public RoundRobinLoadBalancer() {
        this.registryUrl = System.getProperty("REGISTRY_URL", "http://localhost:8080");
        System.out.println("[LOAD-BALANCER] Initialized with registry: " + registryUrl);
    }
    
    /**
     * Obtiene la lista de instancias activas del registry
     */
    @SuppressWarnings("unchecked")
    private List<ServiceInstance> getActiveInstances() {
        try {
            String url = registryUrl + "/registry/instances/active";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("instances")) {
                List<Map<String, Object>> instancesData = (List<Map<String, Object>>) response.get("instances");
                List<ServiceInstance> instances = new ArrayList<>();
                
                for (Map<String, Object> data : instancesData) {
                    ServiceInstance instance = new ServiceInstance();
                    instance.setId((String) data.get("id"));
                    instance.setHost((String) data.get("host"));
                    instance.setPort((Integer) data.get("port"));
                    instance.setStatus((String) data.get("status"));
                    instances.add(instance);
                }
                
                return instances;
            }
        } catch (Exception e) {
            System.err.println("[LOAD-BALANCER] Error fetching instances: " + e.getMessage());
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Selecciona la siguiente instancia usando Round-Robin
     */
    public ServiceInstance getNextInstance() {
        List<ServiceInstance> instances = getActiveInstances();
        
        if (instances.isEmpty()) {
            System.err.println("[LOAD-BALANCER] No active instances available");
            return null;
        }
        
        // Round-Robin: seleccionar la siguiente instancia
        int index = currentIndex.getAndIncrement() % instances.size();
        ServiceInstance selected = instances.get(index);
        
        System.out.println("[LOAD-BALANCER] Selected instance " + (index + 1) + "/" + instances.size() + ": " + selected);
        
        return selected;
    }
    
    /**
     * Reenvía una petición POST a un backend
     */
    public Map<String, Object> forwardPostRequest(String path, Object body) {
        ServiceInstance instance = getNextInstance();
        
        if (instance == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "No backend instances available");
            return error;
        }
        
        try {
            String url = instance.getUrl() + path;
            System.out.println("[LOAD-BALANCER] Forwarding POST to: " + url);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);
            
            // Agregar información del backend que procesó la petición
            if (response != null) {
                response.put("processedBy", instance.getId());
                response.put("backendUrl", instance.getUrl());
            }
            
            return response;
            
        } catch (Exception e) {
            System.err.println("[LOAD-BALANCER] Error forwarding request: " + e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error forwarding request: " + e.getMessage());
            error.put("backend", instance.getId());
            return error;
        }
    }
    
    /**
     * Reenvía una petición GET a un backend
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> forwardGetRequest(String path) {
        ServiceInstance instance = getNextInstance();
        
        if (instance == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "No backend instances available");
            return error;
        }
        
        try {
            String url = instance.getUrl() + path;
            System.out.println("[LOAD-BALANCER] Forwarding GET to: " + url);
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            // Agregar información del backend que procesó la petición
            if (response != null) {
                response.put("processedBy", instance.getId());
                response.put("backendUrl", instance.getUrl());
            }
            
            return response;
            
        } catch (Exception e) {
            System.err.println("[LOAD-BALANCER] Error forwarding request: " + e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error forwarding request: " + e.getMessage());
            error.put("backend", instance.getId());
            return error;
        }
    }
    
    /**
     * Obtiene estadísticas de los backends
     */
    public Map<String, Object> getStats() {
        List<ServiceInstance> instances = getActiveInstances();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBackends", instances.size());
        stats.put("backends", instances);
        stats.put("currentIndex", currentIndex.get());
        stats.put("strategy", "Round-Robin");
        
        return stats;
    }
}