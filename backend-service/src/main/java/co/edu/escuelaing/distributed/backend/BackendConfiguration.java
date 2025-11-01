package co.edu.escuelaing.distributed.backend;

import co.edu.escuelaing.distributed.datastore.ReplicatedHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

/**
 * Configuración de Spring para crear el bean de ReplicatedHashMap
 */
@Configuration
public class BackendConfiguration {
    
    private ReplicatedHashMap dataStore;
    
    @Bean
    public ReplicatedHashMap replicatedHashMap() throws Exception {
        String nodeName = System.getProperty("NODE_NAME", "backend-node-" + System.currentTimeMillis());
        String clusterName = System.getProperty("CLUSTER_NAME", "DataStoreCluster");
        
        System.out.println("[CONFIG] Initializing ReplicatedHashMap...");
        System.out.println("[CONFIG] Node Name: " + nodeName);
        System.out.println("[CONFIG] Cluster Name: " + clusterName);
        
        dataStore = new ReplicatedHashMap(clusterName, nodeName);
        dataStore.start();
        
        System.out.println("[CONFIG] ReplicatedHashMap initialized successfully");
        
        return dataStore;
    }
    
    @PreDestroy
    public void cleanup() {
        if (dataStore != null) {
            System.out.println("[CONFIG] Shutting down ReplicatedHashMap...");
            dataStore.stop();
        }
    }
}