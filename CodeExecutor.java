import java.util.*;
import java.util.regex.*;

public class CodeExecutor {
    private static int nextObjectId = 1;
    
    public List<ExecutionState> execute(String code, String language) throws Exception {
        if ("Java".equals(language)) {
            return executeJava(code);
        } else if ("Python".equals(language)) {
            return executePython(code);
        } else {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }
    
    private List<ExecutionState> executeJava(String code) throws Exception {
        List<ExecutionState> states = new ArrayList<>();
        
        // Extract the class name
        String className = extractJavaClassName(code);
        if (className == null) {
            throw new IllegalArgumentException("Could not find a valid class name in the Java code.");
        }
        
        // Split the code into lines for analysis
        String[] lines = code.split("\n");
        
        // Track variables, scopes, and control flow
        Map<String, Value> localVariables = new HashMap<>();
        Stack<Map<String, Value>> scopeStack = new Stack<>();
        scopeStack.push(localVariables);
        
        // Track function definitions and calls
        Map<String, Integer> functionStartLines = new HashMap<>();
        Map<String, Integer> functionEndLines = new HashMap<>();
        String currentFunction = "main";
        
        // First pass: identify functions
        int bracketCount = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*") || line.startsWith("*")) {
                continue;
            }
            
            // Look for method definitions
<<<<<<< HEAD
            if (line.contains("public") || line.contains("private") || line.contains("protected") || line.contains("default")) {
=======
            if (line.contains("public") || line.contains("private") || line.contains("protected")) {
>>>>>>> 075c35528cb881a3537536e5403360b67ee83f2e
                if (line.contains("(") && line.contains(")") && !line.contains(";")) {
                    // Extract method name
                    Pattern methodPattern = Pattern.compile("(public|private|protected)\\s+\\w+\\s+(\\w+)\\s*\\(");
                    Matcher methodMatcher = methodPattern.matcher(line);
                    if (methodMatcher.find()) {
                        String methodName = methodMatcher.group(2);
                        functionStartLines.put(methodName, i + 1);
                    }
                }
            }
            
            // Track bracket counts to determine function end
            if (line.contains("{")) {
                bracketCount++;
            }
            if (line.contains("}")) {
                bracketCount--;
                if (bracketCount == 0 && !functionStartLines.isEmpty()) {
                    // This might be the end of a function
                    for (String funcName : functionStartLines.keySet()) {
                        if (!functionEndLines.containsKey(funcName)) {
                            functionEndLines.put(funcName, i + 1);
                            break;
                        }
                    }
                }
            }
        }
        
        // Second pass: create execution states
        int lineCount = 0;
        boolean inLoop = false;
        int loopStartLine = 0;
        int loopIterations = 0;
        final int MAX_LOOP_ITERATIONS = 5; // Prevent infinite loops
        
        // Process each line
        while (lineCount < lines.length) {
            lineCount++;
            
            // Check if we're at the end of the file
            if (lineCount > lines.length) break;
            
            String line = lines[lineCount - 1].trim();
            
            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*") || line.startsWith("*")) {
                continue;
            }
            
            // Skip lines with only brackets
            if (line.matches("\\s*[{}]\\s*")) {
                continue;
            }
            
            // Create a new execution state for this line
            ExecutionState state = new ExecutionState(lineCount);
            state.setCode(code); // Set the full code
            
            // Add the current frame
            Frame frame = new Frame(currentFunction, lineCount);
            state.addFrame(frame);
            
            // Only add meaningful code to output, not explanations
            if (line.contains("{") || line.contains("}") || line.contains(";") || 
                line.startsWith("if") || line.startsWith("for") || line.startsWith("while") ||
                line.startsWith("class") || line.startsWith("public") || line.startsWith("private") ||
                line.startsWith("protected") || line.startsWith("return")) {
                // This is actual code, not just a comment or explanation
                state.appendOutput("Line " + lineCount + ": " + line + "\n");
            }
            
            // Check for Scanner or input usage
            if (line.contains("Scanner") || line.contains(".nextLine") || line.contains(".next") || 
                line.contains(".nextInt") || line.contains(".nextDouble") || 
                line.contains("System.in")) {
                // Mark this state as requiring input
                state.appendOutput("[Input required] " + line + "\n");
            }
            
            // Check for function calls
            if (line.contains("(") && line.contains(")") && !line.contains("new ") && 
                !line.startsWith("if") && !line.startsWith("for") && !line.startsWith("while")) {
                
                // Extract potential function name
                Pattern funcCallPattern = Pattern.compile("(\\w+)\\s*\\(");
                Matcher funcCallMatcher = funcCallPattern.matcher(line);
                if (funcCallMatcher.find()) {
                    String calledFunc = funcCallMatcher.group(1);
                    
                    // Check if this is a known function
                    if (functionStartLines.containsKey(calledFunc)) {
                        // Create a new frame for the function call
                        Frame callFrame = new Frame(calledFunc, functionStartLines.get(calledFunc));
                        state.addFrame(callFrame);
                    }
                }
            }
            
            // Check for variable assignments
            if (line.contains("=") && !line.contains("==") && 
                !line.contains(">=") && !line.contains("<=") && 
                !line.contains("!=")) {
                
                try {
                    String[] parts = line.split("=");
                    String varPart = parts[0].trim();
                    String valuePart = parts[1].trim();
                    
                    // Extract variable name (handle declarations)
                    String varName = varPart;
                    if (varPart.contains(" ")) {
                        varName = varPart.substring(varPart.lastIndexOf(" ") + 1);
                    }
                    
                    // Remove any trailing semicolons from value
                    if (valuePart.endsWith(";")) {
                        valuePart = valuePart.substring(0, valuePart.length() - 1).trim();
                    }
                    
                    // Determine value type and create appropriate Value object
                    Value value;
                    if (valuePart.equals("null")) {
                        value = new Value("null");
                    } else if (valuePart.matches("\\d+") || valuePart.matches("\\d+\\.\\d+") ||
                              valuePart.equals("true") || valuePart.equals("false")) {
                        value = new Value(valuePart);
                    } else if (valuePart.contains("new ")) {
                        // It's an object creation
                        int objectId = nextObjectId++;
                        value = new Value(objectId);
                        
                        // Extract object type
                        String objType = "Object";
                        Pattern pattern = Pattern.compile("new\\s+(\\w+)");
                        Matcher matcher = pattern.matcher(valuePart);
                        if (matcher.find()) {
                            objType = matcher.group(1);
                        }
                        
                        // Create a heap object
                        HeapObject obj = new HeapObject(objectId, objType);
                        
                        // Check for array initialization
                        if (valuePart.contains("[") && valuePart.contains("]")) {
                            objType = "Array";
                            obj = new HeapObject(objectId, objType);
                            
                            // Try to extract array elements
                            if (valuePart.contains("{") && valuePart.contains("}")) {
                                Pattern arrayPattern = Pattern.compile("\\{([^}]*)\\}");
                                Matcher arrayMatcher = arrayPattern.matcher(valuePart);
                                if (arrayMatcher.find()) {
                                    String elements = arrayMatcher.group(1);
                                    String[] elementArray = elements.split(",");
                                    
                                    for (int i = 0; i < elementArray.length; i++) {
                                        String element = elementArray[i].trim();
                                        obj.addProperty("[" + i + "]", new Value(element));
                                    }
                                }
                            }
                        }
                        
                        state.addHeapObject(obj);
                    } else {
                        // Check if it's a reference to an existing variable
                        if (localVariables.containsKey(valuePart)) {
                            value = localVariables.get(valuePart);
                        } else {
                            // It's an expression or unknown value
                            value = new Value(valuePart);
                        }
                    }
                    
                    // Add the variable to the frame
                    frame.addVariable(varName, value);
                    
                    // Update our variables map for future states
                    localVariables.put(varName, value);
                } catch (Exception e) {
                    // If parsing fails, just continue
                }
            }
            
            // Check for loops (for, while)
            if (line.startsWith("for") || line.startsWith("while")) {
                if (!inLoop) {
                    inLoop = true;
                    loopStartLine = lineCount;
                    loopIterations = 0;
                }
            }
            
            // Check for loop end (assuming loops end with a closing brace)
            if (inLoop && line.equals("}")) {
                loopIterations++;
                
                if (loopIterations < MAX_LOOP_ITERATIONS) {
                    // Go back to the loop start for another iteration
                    lineCount = loopStartLine;
                } else {
                    // Exit the loop after max iterations
                    inLoop = false;
                }
            }
            
            // Add all current variables to the frame
            for (Map.Entry<String, Value> entry : localVariables.entrySet()) {
                if (!frame.getVariables().containsKey(entry.getKey())) {
                    frame.addVariable(entry.getKey(), entry.getValue());
                }
            }
            
            states.add(state);
        }
        
        // If we couldn't create any meaningful states, create a dummy state
        if (states.isEmpty()) {
            ExecutionState state = new ExecutionState(1);
            state.setCode(code);
            Frame frame = new Frame("main", 1);
            state.addFrame(frame);
            states.add(state);
        }
        
        return states;
    }
    
    private String extractJavaClassName(String code) {
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private List<ExecutionState> executePython(String code) throws Exception {
        List<ExecutionState> states = new ArrayList<>();
        
        // Split the code into lines for analysis
        String[] lines = code.split("\n");
        
        // Track variables, scopes, and indentation
        Map<String, Value> localVariables = new HashMap<>();
        Stack<Map<String, Value>> scopeStack = new Stack<>();
        scopeStack.push(localVariables);
        
        // Track function definitions and calls
        Map<String, Integer> functionStartLines = new HashMap<>();
        Map<String, Integer> functionIndents = new HashMap<>();
        String currentFunction = "global";
        int currentIndent = 0;
        
        // First pass: identify functions and their indentation levels
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // Skip empty lines and comments
            if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                continue;
            }
            
            // Calculate indentation level
            int indent = 0;
            while (indent < line.length() && Character.isWhitespace(line.charAt(indent))) {
                indent++;
            }
            
            // Look for function definitions
            if (line.trim().startsWith("def ")) {
                Pattern funcPattern = Pattern.compile("def\\s+(\\w+)\\s*\\(");
                Matcher funcMatcher = funcPattern.matcher(line);
                if (funcMatcher.find()) {
                    String funcName = funcMatcher.group(1);
                    functionStartLines.put(funcName, i + 1);
                    functionIndents.put(funcName, indent);
                }
            }
        }
        
        // Second pass: create execution states
        int lineCount = 0;
        boolean inLoop = false;
        int loopStartLine = 0;
        int loopIndent = 0;
        int loopIterations = 0;
        final int MAX_LOOP_ITERATIONS = 5; // Prevent infinite loops
        
        // Process each line
        while (lineCount < lines.length) {
            lineCount++;
            
            // Check if we're at the end of the file
            if (lineCount > lines.length) break;
            
            String line = lines[lineCount - 1];
            String trimmedLine = line.trim();
            
            // Skip empty lines and comments
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                continue;
            }
            
            // Calculate indentation level
            int indent = 0;
            while (indent < line.length() && Character.isWhitespace(line.charAt(indent))) {
                indent++;
            }
            
            // Check if we're exiting a function based on indentation
            if (indent < currentIndent && !currentFunction.equals("global")) {
                currentFunction = "global";
                currentIndent = 0;
            }
            
            // Create a new execution state for this line
            ExecutionState state = new ExecutionState(lineCount);
            state.setCode(code);
            
            // Add the current frame
            Frame frame = new Frame(currentFunction, lineCount);
            state.addFrame(frame);
            
            // Check for input functions
            if (trimmedLine.contains("input(") || trimmedLine.contains("raw_input(")) {
                // Mark this state as requiring input
                state.appendOutput("[Input required] " + trimmedLine + "\n");
            }
            
            // Check for function definitions
            if (trimmedLine.startsWith("def ")) {
                Pattern funcPattern = Pattern.compile("def\\s+(\\w+)\\s*\\(");
                Matcher funcMatcher = funcPattern.matcher(trimmedLine);
                if (funcMatcher.find()) {
                    String funcName = funcMatcher.group(1);
                    currentFunction = funcName;
                    currentIndent = indent;
                    
                    // Clear local variables when entering a new function
                    localVariables = new HashMap<>();
                    scopeStack.clear();
                    scopeStack.push(localVariables);
                }
            }
            
            // Check for function calls
            if (trimmedLine.contains("(") && !trimmedLine.startsWith("def ") && 
                !trimmedLine.startsWith("if ") && !trimmedLine.startsWith("for ") && 
                !trimmedLine.startsWith("while ")) {
                
                Pattern funcCallPattern = Pattern.compile("(\\w+)\\s*\\(");
                Matcher funcCallMatcher = funcCallPattern.matcher(trimmedLine);
                if (funcCallMatcher.find()) {
                    String calledFunc = funcCallMatcher.group(1);
                    
                    // Check if this is a known function
                    if (functionStartLines.containsKey(calledFunc)) {
                        // Create a new frame for the function call
                        Frame callFrame = new Frame(calledFunc, functionStartLines.get(calledFunc));
                        state.addFrame(callFrame);
                    }
                }
            }
            
            // Check for variable assignments
            if (trimmedLine.contains("=") && !trimmedLine.contains("==") && 
                !trimmedLine.contains(">=") && !trimmedLine.contains("<=") && 
                !trimmedLine.contains("!=")) {
                
                try {
                    String[] parts = trimmedLine.split("=", 2); // Split only at the first =
                    String varName = parts[0].trim();
                    String valuePart = parts[1].trim();
                    
                    // Determine value type and create appropriate Value object
                    Value value;
                    if (valuePart.equals("None")) {
                        value = new Value("None");
                    } else if (valuePart.matches("\\d+") || valuePart.matches("\\d+\\.\\d+") ||
                              valuePart.equals("True") || valuePart.equals("False")) {
                        value = new Value(valuePart);
                    } else if (valuePart.startsWith("[") && valuePart.endsWith("]")) {
                        // It's a list
                        int objectId = nextObjectId++;
                        value = new Value(objectId);
                        
                        HeapObject obj = new HeapObject(objectId, "list");
                        
                        // Try to extract list elements
                        String elements = valuePart.substring(1, valuePart.length() - 1);
                        if (!elements.trim().isEmpty()) {
                            String[] elementArray = elements.split(",");
                            
                            for (int i = 0; i < elementArray.length; i++) {
                                String element = elementArray[i].trim();
                                if (element.matches("\\d+") || element.matches("\\d+\\.\\d+") ||
                                    element.equals("True") || element.equals("False") || 
                                    element.equals("None")) {
                                    obj.addProperty("[" + i + "]", new Value(element));
                                } else {
                                    // It might be a reference to another variable
                                    if (localVariables.containsKey(element)) {
                                        obj.addProperty("[" + i + "]", localVariables.get(element));
                                    } else {
                                        obj.addProperty("[" + i + "]", new Value(element));
                                    }
                                }
                            }
                        }
                        
                        state.addHeapObject(obj);
                    } else if (valuePart.startsWith("{") && valuePart.endsWith("}")) {
                        // It's a dictionary
                        int objectId = nextObjectId++;
                        value = new Value(objectId);
                        
                        HeapObject obj = new HeapObject(objectId, "dict");
                        state.addHeapObject(obj);
                    } else {
                        // Check if it's a reference to an existing variable
                        if (localVariables.containsKey(valuePart)) {
                            value = localVariables.get(valuePart);
                        } else {
                            // It's an expression or unknown value
                            value = new Value(valuePart);
                        }
                    }
                    
                    // Add the variable to the frame
                    frame.addVariable(varName, value);
                    
                    // Update our variables map for future states
                    localVariables.put(varName, value);
                } catch (Exception e) {
                    // If parsing fails, just continue
                }
            }
            
            // Check for loops (for, while)
            if (trimmedLine.startsWith("for ") || trimmedLine.startsWith("while ")) {
                if (!inLoop) {
                    inLoop = true;
                    loopStartLine = lineCount;
                    loopIndent = indent;
                    loopIterations = 0;
                }
            }
            
            // Check for loop end (based on indentation)
            if (inLoop && indent <= loopIndent && lineCount > loopStartLine) {
                loopIterations++;
                
                if (loopIterations < MAX_LOOP_ITERATIONS) {
                    // Go back to the loop start for another iteration
                    lineCount = loopStartLine;
                } else {
                    // Exit the loop after max iterations
                    inLoop = false;
                }
            }
            
            // Add all current variables to the frame
            for (Map.Entry<String, Value> entry : localVariables.entrySet()) {
                if (!frame.getVariables().containsKey(entry.getKey())) {
                    frame.addVariable(entry.getKey(), entry.getValue());
                }
            }
            
            states.add(state);
        }
        
        // If we couldn't create any meaningful states, create a dummy state
        if (states.isEmpty()) {
            ExecutionState state = new ExecutionState(1);
            state.setCode(code);
            Frame frame = new Frame("global", 1);
            state.addFrame(frame);
            states.add(state);
        }
        
        return states;
    }
}
