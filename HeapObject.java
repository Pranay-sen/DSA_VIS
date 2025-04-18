import java.util.LinkedHashMap;
import java.util.Map;

public class HeapObject {
    private int id;
    private String type;
    private Map<String, Value> properties;
    
    public HeapObject(int id, String type) {
        this.id = id;
        this.type = type;
        this.properties = new LinkedHashMap<>(); // Preserves insertion order
    }
    
    public int getId() {
        return id;
    }
    
    public String getType() {
        return type;
    }
    
    public Map<String, Value> getProperties() {
        return properties;
    }
    
    public void addProperty(String name, Value value) {
        properties.put(name, value);
    }
    
    public Value getProperty(String name) {
        return properties.get(name);
    }
}
