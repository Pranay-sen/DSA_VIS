import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Rectangle;

public class ExecutionState {
    private int lineNumber;
    private List<Frame> frames;
    private List<HeapObject> heapObjects;
    private String output;
    private Map<Frame, Rectangle> framePositions;
    private Map<HeapObject, Rectangle> objectPositions;
    private String code;
    
    public ExecutionState(int lineNumber) {
        this.lineNumber = lineNumber;
        this.frames = new ArrayList<>();
        this.heapObjects = new ArrayList<>();
        this.output = "";
        this.framePositions = new HashMap<>();
        this.objectPositions = new HashMap<>();
        this.code = "";
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public List<Frame> getFrames() {
        return frames;
    }
    
    public void addFrame(Frame frame) {
        frames.add(frame);
    }
    
    public List<HeapObject> getHeapObjects() {
        return heapObjects;
    }
    
    public void addHeapObject(HeapObject object) {
        heapObjects.add(object);
    }
    
    public String getOutput() {
        return output;
    }
    
    public void setOutput(String output) {
        this.output = output;
    }
    
    public void appendOutput(String text) {
        this.output += text;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public Map<Frame, Rectangle> getFramePositions() {
        return framePositions;
    }
    
    public void setFramePositions(Map<Frame, Rectangle> framePositions) {
        this.framePositions = framePositions;
    }
    
    public Map<HeapObject, Rectangle> getObjectPositions() {
        return objectPositions;
    }
    
    public void setObjectPositions(Map<HeapObject, Rectangle> objectPositions) {
        this.objectPositions = objectPositions;
    }
}
