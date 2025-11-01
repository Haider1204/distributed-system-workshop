package co.edu.escuelaing.distributed.registry;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio que mantiene el registro de instancias backend
 */
@Service
public class RegistryService {
    
    // Mapa de instancias registradas: ID -> ServiceInstance
    private final Map<String, ServiceInstance> instances = new ConcurrentHashMap<>();
    
    // Tiempo máximo sin heartbeat antes de marcar como inactivo (30 segundos)
    private static final long HEARTBEAT_TIMEOUT = 1200000;
    
    /**
     * Registra una nueva instancia de servicio
     */
    public ServiceInstance register(String id, String host, int port) {
        ServiceInstance instance = new ServiceInstance(id, host, port);
        instances.put(id, instance);
        
        System.out.println("[REGISTRY] Service registered: " + instance);
        System.out.println("[REGISTRY] Total active services: " + getActiveInstances().size());
        
        return instance;
    }
    
    /**
     * Desregistra una instancia
     */
    public boolean unregister(String id) {
        ServiceInstance removed = instances.remove(id);
        if (removed != null) {
            System.out.println("[REGISTRY] Service unregistered: " + removed);
            return true;
        }
        return false;
    }
    
    /**
     * Actualiza el heartbeat de una instancia
     */
    public boolean heartbeat(String id) {
        ServiceInstance instance = instances.get(id);
        if (instance != null) {
            instance.setLastHeartbeat(System.currentTimeMillis());
            instance.setStatus("ACTIVE");
            return true;
        }
        return false;
    }
    
    /**
     * Obtiene todas las instancias registradas
     */
    public List<ServiceInstance> getAllInstances() {
        return new ArrayList<>(instances.values());
    }
    
    /**
     * Obtiene solo las instancias activas
     */
    public List<ServiceInstance> getActiveInstances() {
        long now = System.currentTimeMillis();
        List<ServiceInstance> active = new ArrayList<>();
        
        for (ServiceInstance instance : instances.values()) {
            // Verificar si el heartbeat es reciente
            if ((now - instance.getLastHeartbeat()) < HEARTBEAT_TIMEOUT) {
                instance.setStatus("ACTIVE");
                active.add(instance);
            } else {
                instance.setStatus("INACTIVE");
            }
        }
        
        return active;
    }
    
    /**
     * Obtiene una instancia por ID
     */
    public ServiceInstance getInstance(String id) {
        return instances.get(id);
    }
    
    /**
     * Limpia instancias inactivas
     */
    public int cleanupInactiveInstances() {
        long now = System.currentTimeMillis();
        int removed = 0;
        
        Iterator<Map.Entry<String, ServiceInstance>> iterator = instances.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ServiceInstance> entry = iterator.next();
            ServiceInstance instance = entry.getValue();
            
            // Remover si no ha enviado heartbeat en mucho tiempo (60 segundos)
            if ((now - instance.getLastHeartbeat()) > (HEARTBEAT_TIMEOUT * 3)) {
                System.out.println("[REGISTRY] Removing inactive service: " + instance);
                iterator.remove();
                removed++;
            }
        }
        
        return removed;
    }
}