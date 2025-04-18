import java.util.LinkedHashMap;
import java.util.Map;

public class Frame {
    private String name;
    private int lineNumber;
    private Map<String, Value> variables;
    
    public Frame(String name, int lineNumber) {
        this.name = name;
        this.lineNumber = lineNumber;
        this.variables = new LinkedHashMap<>(); // Preserves insertion order
    }
    
    public String getName() {
        return name;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public Map<String, Value> getVariables() {
        return variables;
    }
    
    public void addVariable(String name, Value value) {
        variables.put(name, value);
    }
    
    public Value getVariable(String name) {
        return variables.get(name);
    }
}
