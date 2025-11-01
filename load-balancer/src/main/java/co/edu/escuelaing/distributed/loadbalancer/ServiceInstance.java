package co.edu.escuelaing.distributed.loadbalancer;

/**
 * Representa una instancia de servicio backend
 */
public class ServiceInstance {
    private String id;
    private String host;
    private int port;
    private String status;
    private long registeredAt;
    private long lastHeartbeat;
    
    public ServiceInstance() {}
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getRegisteredAt() {
        return registeredAt;
    }
    
    public void setRegisteredAt(long registeredAt) {
        this.registeredAt = registeredAt;
    }
    
    public long getLastHeartbeat() {
        return lastHeartbeat;
    }
    
    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
    
    public String getUrl() {
        return "http://" + host + ":" + port;
    }
    
    @Override
    public String toString() {
        return "ServiceInstance{id='" + id + "', host='" + host + "', port=" + port + "}";
    }
}