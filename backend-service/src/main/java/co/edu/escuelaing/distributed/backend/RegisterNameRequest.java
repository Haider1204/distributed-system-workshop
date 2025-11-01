package co.edu.escuelaing.distributed.backend;

/**
 * DTO para recibir solicitudes de registro de nombres
 */
public class RegisterNameRequest {
    private String name;
    
    public RegisterNameRequest() {}
    
    public RegisterNameRequest(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}