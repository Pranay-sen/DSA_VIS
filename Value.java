public class Value {
    private enum Type {
        PRIMITIVE, REFERENCE
    }
    
    private Type type;
    private String primitiveValue;
    private int referenceId;
    
    // Constructor for primitive values
    public Value(String primitiveValue) {
        this.type = Type.PRIMITIVE;
        this.primitiveValue = primitiveValue;
        this.referenceId = -1;
    }
    
    // Constructor for reference values
    public Value(int referenceId) {
        this.type = Type.REFERENCE;
        this.primitiveValue = null;
        this.referenceId = referenceId;
    }
    
    public boolean isReference() {
        return type == Type.REFERENCE;
    }
    
    public String getPrimitiveValue() {
        return primitiveValue;
    }
    
    public int getReference() {
        return referenceId;
    }
    
    public String getValue() {
        return isReference() ? "ref" : primitiveValue;
    }
    
    @Override
    public String toString() {
        if (type == Type.PRIMITIVE) {
            return primitiveValue;
        } else {
            return "ref " + referenceId;
        }
    }
}
