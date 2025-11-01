package co.edu.escuelaing.distributed.datastore;

import org.jgroups.*;
import org.jgroups.util.Util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Replicated HashMap using JGroups for distributed data storage
 * Adapted from SimpleChat tutorial to use HashMap instead of List
 */
public class ReplicatedHashMap implements Receiver {
    
    // Thread-safe HashMap para almacenar nombres y timestamps
    private final Map<String, Long> dataStore = new ConcurrentHashMap<>();
    
    // Canal de comunicación JGroups
    private JChannel channel;
    
    // Nombre del cluster
    private final String clusterName;
    
    // Nombre del nodo (identificador único)
    private final String nodeName;
    
    /**
     * Constructor
     * @param clusterName Nombre del cluster a unirse
     * @param nodeName Nombre único de este nodo
     */
    public ReplicatedHashMap(String clusterName, String nodeName) {
        this.clusterName = clusterName;
        this.nodeName = nodeName;
    }
    
    /**
     * Inicia el canal JGroups y se une al cluster
     */
    public void start() throws Exception {
        System.out.println("[" + nodeName + "] Starting ReplicatedHashMap...");
        
        // Obtener configuración de red
        String externalAddr = System.getProperty("JGROUPS_EXTERNAL_ADDR");
        String tcpPort = System.getProperty("JGROUPS_TCP_PORT", "7800");
        String initialHosts = System.getProperty("JGROUPS_TCPPING_INITIAL_HOSTS", "localhost[7800]");
        
        System.out.println("[" + nodeName + "] JGroups configuration:");
        System.out.println("[" + nodeName + "]   External address: " + externalAddr);
        System.out.println("[" + nodeName + "]   TCP port: " + tcpPort);
        System.out.println("[" + nodeName + "]   Initial hosts: " + initialHosts);
        
        // Si se especifica external_addr, usamos configuración personalizada
        if (externalAddr != null && !externalAddr.isEmpty()) {
            String config = "<config xmlns=\"urn:org:jgroups\"\n" +
                    "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "        xsi:schemaLocation=\"urn:org:jgroups http://www.jgroups.org/schema/jgroups.xsd\">\n" +
                    "    <TCP bind_port=\"" + tcpPort + "\"\n" +
                    "         external_addr=\"" + externalAddr + "\"\n" +
                    "         recv_buf_size=\"5000000\"\n" +
                    "         send_buf_size=\"5000000\"/>\n" +
                    "    <TCPPING async_discovery=\"true\"\n" +
                    "             initial_hosts=\"" + initialHosts + "\"\n" +
                    "             port_range=\"0\"/>\n" +
                    "    <MERGE3/>\n" +
                    "    <FD_SOCK/>\n" +
                    "    <FD_ALL/>\n" +
                    "    <VERIFY_SUSPECT/>\n" +
                    "    <pbcast.NAKACK2 use_mcast_xmit=\"false\"/>\n" +
                    "    <UNICAST3/>\n" +
                    "    <pbcast.STABLE/>\n" +
                    "    <pbcast.GMS print_local_addr=\"true\"/>\n" +
                    "    <UFC/>\n" +
                    "    <MFC/>\n" +
                    "    <FRAG2/>\n" +
                    "    <pbcast.STATE_TRANSFER/>\n" +
                    "</config>";
            
            channel = new JChannel(new ByteArrayInputStream(config.getBytes()));
        } else {
            // Para local, usar configuración simple con TCPPING
            String config = "<config xmlns=\"urn:org:jgroups\"\n" +
                    "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "        xsi:schemaLocation=\"urn:org:jgroups http://www.jgroups.org/schema/jgroups.xsd\">\n" +
                    "    <TCP bind_port=\"" + tcpPort + "\"/>\n" +
                    "    <TCPPING async_discovery=\"true\"\n" +
                    "             initial_hosts=\"" + initialHosts + "\"\n" +
                    "             port_range=\"0\"/>\n" +
                    "    <MERGE3/>\n" +
                    "    <FD_SOCK/>\n" +
                    "    <FD_ALL/>\n" +
                    "    <VERIFY_SUSPECT/>\n" +
                    "    <pbcast.NAKACK2 use_mcast_xmit=\"false\"/>\n" +
                    "    <UNICAST3/>\n" +
                    "    <pbcast.STABLE/>\n" +
                    "    <pbcast.GMS print_local_addr=\"true\"/>\n" +
                    "    <UFC/>\n" +
                    "    <MFC/>\n" +
                    "    <FRAG2/>\n" +
                    "    <pbcast.STATE_TRANSFER/>\n" +
                    "</config>";
            
            channel = new JChannel(new ByteArrayInputStream(config.getBytes()));
        }
        
        channel.setReceiver(this);
        
        // Conectar al cluster
        channel.connect(clusterName);
        System.out.println("[" + nodeName + "] Connected to cluster: " + clusterName);
        System.out.println("[" + nodeName + "] Local address: " + channel.getAddress());
        
        // Obtener estado actual del cluster (si hay otros nodos)
        channel.getState(null, 10000);
        System.out.println("[" + nodeName + "] State retrieved from cluster");
    }
    
    
    /**
     * Cierra el canal
     */
    public void stop() {
        if (channel != null && channel.isConnected()) {
            channel.close();
            System.out.println("[" + nodeName + "] Channel closed");
        }
    }
    
    /**
     * Almacena un par clave-valor y lo replica a todos los nodos
     * @param key Nombre a registrar
     * @param timestamp Timestamp del registro
     */
    public void put(String key, Long timestamp) throws Exception {
        // Crear objeto de operación
        DataOperation operation = new DataOperation("PUT", key, timestamp);
        
        // Enviar a todos los nodos (incluyendo este)
        Message msg = new ObjectMessage(null, operation);
        channel.send(msg);
        
        System.out.println("[" + nodeName + "] Sent PUT operation: " + key + " -> " + timestamp);
    }
    
    /**
     * Obtiene un valor del HashMap local
     * @param key Clave a buscar
     * @return Timestamp asociado o null
     */
    public Long get(String key) {
        return dataStore.get(key);
    }
    
    /**
     * Obtiene todos los datos almacenados
     * @return Copia del HashMap
     */
    public Map<String, Long> getAll() {
        return new HashMap<>(dataStore);
    }
    
    /**
     * Obtiene el tamaño del HashMap
     */
    public int size() {
        return dataStore.size();
    }
    
    /**
     * Callback cuando cambia la vista del cluster (nodos se unen/salen)
     */
    @Override
    public void viewAccepted(View new_view) {
        System.out.println("[" + nodeName + "] ** View changed: " + new_view);
        System.out.println("[" + nodeName + "] ** Active members: " + new_view.size());
    }
    
    /**
     * Callback cuando se recibe un mensaje de otro nodo
     */
    @Override
    public void receive(Message msg) {
        try {
            DataOperation operation = (DataOperation) msg.getObject();
            
            // Aplicar operación localmente
            if ("PUT".equals(operation.getOperation())) {
                dataStore.put(operation.getKey(), operation.getTimestamp());
                System.out.println("[" + nodeName + "] Received and applied: " + 
                    operation.getKey() + " -> " + operation.getTimestamp() + 
                    " (from " + msg.getSrc() + ")");
            }
        } catch (Exception e) {
            System.err.println("[" + nodeName + "] Error receiving message: " + e.getMessage());
        }
    }
    
    /**
     * Callback para enviar el estado actual a un nuevo nodo
     */
    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized(dataStore) {
            System.out.println("[" + nodeName + "] Sending state (" + dataStore.size() + " entries) to new member");
            Util.objectToStream(new HashMap<>(dataStore), new DataOutputStream(output));
        }
    }
    
    /**
     * Callback para recibir el estado de otro nodo (cuando nos unimos)
     */
    @Override
    public void setState(InputStream input) throws Exception {
        Map<String, Long> receivedState = (Map<String, Long>) Util.objectFromStream(new DataInputStream(input));
        
        synchronized(dataStore) {
            dataStore.clear();
            dataStore.putAll(receivedState);
        }
        
        System.out.println("[" + nodeName + "] State received: " + dataStore.size() + " entries");
        System.out.println("[" + nodeName + "] Current data:");
        dataStore.forEach((k, v) -> System.out.println("  - " + k + " : " + v));
    }
    
    /**
     * Clase interna para representar operaciones de datos
     */
    private static class DataOperation implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final String operation;  // "PUT", "DELETE", etc.
        private final String key;
        private final Long timestamp;
        
        public DataOperation(String operation, String key, Long timestamp) {
            this.operation = operation;
            this.key = key;
            this.timestamp = timestamp;
        }
        
        public String getOperation() { return operation; }
        public String getKey() { return key; }
        public Long getTimestamp() { return timestamp; }
    }
    
    /**
     * Método de prueba para verificar funcionamiento
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java ReplicatedHashMap <node-name>");
            System.exit(1);
        }
        
        String nodeName = args[0];
        ReplicatedHashMap map = new ReplicatedHashMap("DataStoreCluster", nodeName);
        
        try {
            map.start();
            
            // Prueba: insertar algunos datos
            System.out.println("\n[" + nodeName + "] Press Enter to add test data...");
            System.in.read();
            
            map.put("Alice", System.currentTimeMillis());
            Thread.sleep(1000);
            map.put("Bob", System.currentTimeMillis());
            Thread.sleep(1000);
            map.put("Charlie", System.currentTimeMillis());
            
            System.out.println("\n[" + nodeName + "] Current data store:");
            map.getAll().forEach((k, v) -> System.out.println("  " + k + " : " + v));
            
            // Mantener corriendo
            System.out.println("\n[" + nodeName + "] Press Enter to exit...");
            System.in.read();
            
        } finally {
            map.stop();
        }
    }
}