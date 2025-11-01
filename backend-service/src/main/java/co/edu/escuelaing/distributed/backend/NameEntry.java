package co.edu.escuelaing.distributed.backend;

/**
 * Modelo que representa una entrada de nombre con timestamp
 */
public class NameEntry {
    private String name;
    private Long timestamp;
    
    public NameEntry() {}
    
    public NameEntry(String name, Long timestamp) {
        this.name = name;
        this.timestamp = timestamp;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "NameEntry{name='" + name + "', timestamp=" + timestamp + "}";
    }
}