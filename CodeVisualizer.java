import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Utilities;

import org.w3c.dom.events.MouseEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

public class CodeVisualizer extends JFrame {
    // UI Components
    private JTextPane codeEditor;
    private JComboBox<String> languageSelector;
    private JButton visualizeButton;
    private JButton runButton;
    private JButton firstButton;
    private JButton nextButton;
    private JButton lastButton;
    private JPanel visualizationPanel;
    private JTextArea outputArea;
    private JTextField inputField;
    private JLabel stepLabel;
    private JPanel codeHighlightPanel;
    private JTextArea codeDisplayArea;
    private JButton previousButton;
    
    // Background image
    private BufferedImage backgroundImage;
    private float backgroundOpacity = 0.2f; // Light opacity (20%)
    
    // Execution state
    private List<ExecutionState> executionStates;
    private int currentStep;
    private CodeExecutor codeExecutor;
    private String[] codeLines;
    private int currentLine;
    private boolean isVisualizing;
    private Set<Integer> persistentObjectIds = new HashSet<>();
    
    // Colors and styling
    // Colors for visualization on white background (original style)
    private static final Color HIGHLIGHT_COLOR = new Color(220, 240, 255);  // Light blue highlight
    private static final Color ARROW_COLOR = new Color(255, 50, 50);  // Red arrow
    private static final Color FRAME_COLOR = new Color(200, 220, 240);  // Light blue frame
    private static final Color OBJECT_COLOR = new Color(255, 255, 200);  // Light yellow object
    private static final Color TEXT_COLOR = new Color(0, 0, 0);  // Black text
    private static final Color OUTLINE_COLOR = new Color(0, 120, 215);  // Blue outline
    
    // Fonts
    private static final Font CODE_FONT = new Font("JetBrains Mono", Font.PLAIN, 14); 
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 13);      
    private static final Font VARIABLE_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    
    public CodeVisualizer() {
        setTitle("Code Visualizer - Python Tutor Clone");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Load background image
        try {
            // Try multiple locations to find the image
            String filename = "WhatsApp Image 2025-04-17 at 22.05.59_09c7f6c7.jpg";
            File imageFile = null;
            
            // Possible locations to check
            String[] possiblePaths = {
                filename, // Current directory
                "../" + filename, // Parent directory
                "../../" + filename, // Two levels up
                System.getProperty("user.dir") + "/" + filename, // User directory
                System.getProperty("user.dir") + "/../" + filename // Parent of user directory
            };
            
            for (String path : possiblePaths) {
                File testFile = new File(path);
                System.out.println("Testing path: " + testFile.getAbsolutePath());
                if (testFile.exists()) {
                    imageFile = testFile;
                    System.out.println("Found image at: " + imageFile.getAbsolutePath());
                    break;
                }
            }
            
            if (imageFile != null && imageFile.exists()) {
                backgroundImage = ImageIO.read(imageFile);
                System.out.println("Successfully loaded image");
            } else {
                System.err.println("Could not find the image file in any of the checked locations");
            }
        } catch (IOException e) {
            System.err.println("Failed to load background image: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Initialize components
        initComponents();
        
        // Set up auto-completion and smart indentation
        setupEditorAutoFeatures();
        
        // Set up the layout
        setupLayout();
        
        // Add event listeners
        addEventListeners();
        
        // Initialize the code executor
        codeExecutor = new CodeExecutor();
        executionStates = new ArrayList<>();
        currentStep = 0;
        isVisualizing = false;
    }

    private void setupEditorAutoFeatures() {
        codeEditor.addKeyListener(new KeyAdapter() {
            private boolean isAutoCompletionEnabled = true;
            
            @Override
            public void keyTyped(KeyEvent e) {
                try {
                    int caretPos = codeEditor.getCaretPosition();
                    String text = codeEditor.getText();
                    char c = e.getKeyChar();
                    
                    // Handle curly braces specially
                    if (c == '{' && isAutoCompletionEnabled) {
                        e.consume();
                        handleCurlyBraceInsertion(caretPos);
                        return;
                    }
                    
                    // Handle other auto-closing pairs
                    if (isAutoCompletionEnabled && !e.isAltDown() && !e.isControlDown() && !e.isMetaDown()) {
                        if (c == '[' || c == '(' || c == '\'' || c == '"') {
                            e.consume();
                            insertAutoClosingPair(c, caretPos);
                        }
                        else if (c == '}' || c == ']' || c == ')' || c == '\'' || c == '"') {
                            handleClosingCharacter(c, caretPos, text);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                try {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        e.consume();
                        handleEnterKey();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    
    private void handleCurlyBraceInsertion(int caretPos) throws BadLocationException {
        String indentation = getCurrentIndentation(caretPos);
        String toInsert = "{\n" + indentation + "    \n" + indentation + "}";
        
        codeEditor.getDocument().insertString(caretPos, toInsert, null);
        codeEditor.setCaretPosition(caretPos + indentation.length() + 5); // Cursor middle line pe
    }
    
    private void insertAutoClosingPair(char openingChar, int caretPos) throws BadLocationException {
        String toInsert;
        switch (openingChar) {
            case '[': toInsert = "[]"; break;
            case '(': toInsert = "()"; break;
            case '\'': toInsert = "''"; break;
            case '"': toInsert = "\"\""; break;
            default: toInsert = String.valueOf(openingChar);
        }
        codeEditor.getDocument().insertString(caretPos, toInsert, null);
        codeEditor.setCaretPosition(caretPos + 1);
    }
    
    private void handleClosingCharacter(char closingChar, int caretPos, String text) {
        if (caretPos < text.length() && text.charAt(caretPos) == closingChar) {
            codeEditor.setCaretPosition(caretPos + 1);
        }
    }
    
    private void handleEnterKey() throws BadLocationException {
        int caretPos = codeEditor.getCaretPosition();
        String text = codeEditor.getText();
        String indentation = getCurrentIndentation(caretPos);
        
        // Check if we're between curly braces
        if (caretPos > 0 && caretPos < text.length() && 
            text.charAt(caretPos-1) == '{' && text.charAt(caretPos) == '}') {
            // Special handling between {}
            String toInsert = "\n" + indentation + "    ";
            codeEditor.getDocument().insertString(caretPos, toInsert, null);
            codeEditor.setCaretPosition(caretPos + toInsert.length());
        }
        else {
            // Normal enter handling
            String toInsert = "\n" + indentation;
            codeEditor.getDocument().insertString(caretPos, toInsert, null);
            codeEditor.setCaretPosition(caretPos + toInsert.length());
        }
    }
    
    private String getCurrentIndentation(int caretPos) throws BadLocationException {
        int lineStart = Utilities.getRowStart(codeEditor, caretPos);
        int lineEnd = Utilities.getRowEnd(codeEditor, caretPos);
        String currentLine = codeEditor.getText(lineStart, lineEnd - lineStart);
        
        StringBuilder indentation = new StringBuilder();
        for (int i = 0; i < currentLine.length(); i++) {
            if (Character.isWhitespace(currentLine.charAt(i))) {
                indentation.append(currentLine.charAt(i));
            } else {
                break;
            }
        }
        return indentation.toString();
    }
    
    private void initComponents() {
        // Code editor
        codeEditor = new JTextPane();
        codeEditor.setFont(CODE_FONT);
        
        // Add syntax highlighting
        new SyntaxHighlighter(codeEditor);
        
        // Add a line number component to the code editor
        JScrollPane codeScrollPane = new JScrollPane(codeEditor);
        LineNumberComponent lineNumbers = new LineNumberComponent(codeEditor);
        
        // Add a red arrow indicator to the right side of the code editor
        JPanel arrowPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (isVisualizing && currentLine > 0 && currentLine <= codeLines.length) {
                    // Skip empty lines and lines with only brackets
                    String currentLineText = codeLines[currentLine - 1].trim();
                    if (currentLineText.isEmpty() || currentLineText.matches("\\s*[{}]\\s*")) {
                        return;
                    }
                    
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Calculate the y position for the current line
                    int lineHeight = g.getFontMetrics(CODE_FONT).getHeight();
                    int y = (currentLine - 1) * lineHeight + lineHeight / 2;
                    
                    // Draw the red arrow
                    g2d.setColor(ARROW_COLOR);
                    int[] xPoints = {0, 15, 0};
                    int[] yPoints = {y - 5, y, y + 5};
                    g2d.fillPolygon(xPoints, yPoints, 3);
                }
            }
        };
        arrowPanel.setPreferredSize(new Dimension(20, 0));
        
        // Create a panel to hold both line numbers and arrow
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(lineNumbers, BorderLayout.WEST);
        leftPanel.add(arrowPanel, BorderLayout.EAST);
        
        // Set the row header view to include both line numbers and arrow
        codeScrollPane.setRowHeaderView(leftPanel);
        
        // Code display area for visualization (with line numbers)
        codeDisplayArea = new JTextArea();
        codeDisplayArea.setFont(CODE_FONT);
        codeDisplayArea.setEditable(false);
        codeDisplayArea.setMargin(new Insets(5, 5, 5, 5));
        
        // Code highlight panel (contains code display and line highlight)
        codeHighlightPanel = new JPanel();
        codeHighlightPanel.setLayout(new BorderLayout());
        codeHighlightPanel.add(new JScrollPane(codeDisplayArea), BorderLayout.CENTER);
        
        // Language selector
        languageSelector = new JComboBox<>(new String[]{"Java", "Python","C++","Ruby","C","Rust","Go","Node.js"});
        
        // Buttons
        visualizeButton = new JButton("Visualize");
        visualizeButton.setFont(LABEL_FONT);
        runButton = new JButton("Run");
        runButton.setFont(LABEL_FONT);
        firstButton = new JButton("<< First");
        previousButton = new JButton("Previous <");
        previousButton.setFont(LABEL_FONT);
        firstButton.setFont(LABEL_FONT);
        nextButton = new JButton("Next >");
        nextButton.setFont(LABEL_FONT);
        lastButton = new JButton("Last >>");
        lastButton.setFont(LABEL_FONT);
        
        // Step label
        stepLabel = new JLabel("Step 0 of 0");
        stepLabel.setFont(LABEL_FONT);
        
        // Visualization panel
        visualizationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                
                // Set rendering hints for better quality
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Fill with white background as it was originally
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw the visualization with clear colors that stand out on white background
                if (isVisualizing && !executionStates.isEmpty() && currentStep < executionStates.size()) {
                    drawVisualization(g2d, executionStates.get(currentStep));
                }
            }
        };
        
        // Set the visualization panel's preferred size and border
        visualizationPanel.setPreferredSize(new Dimension(600, 450));
        visualizationPanel.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70), 1));
        
        // Output area - make it bigger
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        outputArea.setMargin(new Insets(10, 10, 10, 10));
        
        // Input field
        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.BOLD, 12));
    }
    
    private void setupLayout() {
        Color darkBg = Color.BLACK;
        Color lightGray = Color.WHITE;
        Color buttonBg = new Color(60, 60, 70);
        Color buttonHover = new Color(80, 80, 90);
        Color borderColor = new Color(70, 70, 80);
        Color test = new Color(135, 206, 250);
    
        JPanel backgroundPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                if (backgroundImage != null) {
                    Composite originalComposite = g2d.getComposite();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    g2d.setComposite(originalComposite);
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        
        // Execution Panel
        JPanel executionPanel = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                if (backgroundImage != null) {
                    Composite originalComposite = g2d.getComposite();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    g2d.setComposite(originalComposite);
                }
            }
        };
        executionPanel.setOpaque(false);
        executionPanel.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        
        // Navigation Panel
        JPanel navigationPanel = new JPanel();
        navigationPanel.setOpaque(false);
        navigationPanel.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        
        // ExecutionPanel ke andar navigationPanel add kar
        executionPanel.add(navigationPanel, BorderLayout.SOUTH);
        
        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setOpaque(false);
        
        // executionPanel ko mainPanel ya backgroundPanel me add karna na bhool
        mainPanel.add(executionPanel, BorderLayout.CENTER);
        backgroundPanel.add(mainPanel, BorderLayout.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        topPanel.setOpaque(false);
    
        JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        languagePanel.setOpaque(false);
    
        JLabel langLabel = new JLabel("Language:");
        langLabel.setForeground(lightGray);
        langLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        languagePanel.add(langLabel);
    
        languageSelector.setBackground(new Color(50, 50, 60));
        languageSelector.setForeground(Color.RED);
        languageSelector.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        languageSelector.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        languagePanel.add(languageSelector);
        topPanel.add(languagePanel, BorderLayout.WEST);
    
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
    
        Consumer<JButton> styleButton = btn -> {
            btn.setBackground(buttonBg);
            btn.setForeground(Color.RED);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
            ));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(buttonHover);
                }
                public void mouseExited(MouseEvent e) {
                    btn.setBackground(buttonBg);
                }
            });
        };
    
        styleButton.accept(runButton);
        styleButton.accept(visualizeButton);
        buttonPanel.add(runButton);
        buttonPanel.add(visualizeButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);
    
        JScrollPane codeScrollPane = (JScrollPane) codeEditor.getParent().getParent();
        codeScrollPane.setPreferredSize(new Dimension(500, 450));
        codeScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Removed border here
        codeScrollPane.setOpaque(false);
        codeScrollPane.getViewport().setOpaque(false);
    
        JPanel codePanel = new JPanel(new BorderLayout());
        codePanel.setOpaque(false);
        codePanel.setBorder(new EmptyBorder(0, 0, 10, 0));
    
        JLabel codeLabel = new JLabel("Code Editor");
        codeLabel.setForeground(test);
        codeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        codeLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        codePanel.add(codeLabel, BorderLayout.NORTH);
        codePanel.add(codeScrollPane, BorderLayout.CENTER);
    
        visualizationPanel.setBorder(BorderFactory.createEmptyBorder()); // Removed border here
        JScrollPane visualizationScrollPane = new JScrollPane(visualizationPanel);
        visualizationScrollPane.setPreferredSize(new Dimension(600, 450));
        visualizationScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Removed border here
        visualizationScrollPane.setOpaque(false);
        visualizationScrollPane.getViewport().setOpaque(false);
    
        JPanel visPanel = new JPanel(new BorderLayout());
        visPanel.setOpaque(false);
        visPanel.setBorder(new EmptyBorder(0, 10, 10, 0));
    
        JLabel visLabel = new JLabel("Execution Visualization");
        visLabel.setForeground(test);
        visLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        visLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        visPanel.add(visLabel, BorderLayout.NORTH);
        visPanel.add(visualizationScrollPane, BorderLayout.CENTER);
    
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        controlsPanel.setOpaque(false);
    
        Consumer<JButton> styleNavButton = btn -> {
            btn.setBackground(buttonBg);
            btn.setForeground(darkBg);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
            ));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(buttonHover);
                }
                public void mouseExited(MouseEvent e) {
                    btn.setBackground(buttonBg);
                }
            });
        };
    
        styleNavButton.accept(firstButton);
        styleNavButton.accept(previousButton);
        styleNavButton.accept(nextButton);
        styleNavButton.accept(lastButton);
    
        stepLabel.setForeground(lightGray);
        stepLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        stepLabel.setBorder(new EmptyBorder(0, 15, 0, 15));
    
        controlsPanel.add(firstButton);
        controlsPanel.add(previousButton);
        controlsPanel.add(stepLabel);
        controlsPanel.add(nextButton);
        controlsPanel.add(lastButton);
    
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setOpaque(false);
        outputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
    
        JLabel outputLabel = new JLabel("Program Output");
        outputLabel.setForeground(test);
        outputLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        outputLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
    
        outputArea.setForeground(darkBg);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setPreferredSize(new Dimension(600, 200));
        outputScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Removed border here
        outputScrollPane.setOpaque(false);
        outputScrollPane.getViewport().setOpaque(false);
        
        outputPanel.add(outputLabel, BorderLayout.NORTH);
        outputPanel.add(outputScrollPane, BorderLayout.CENTER);
    
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
    
        JLabel inputLabel = new JLabel("Input");
        inputLabel.setForeground(test);
        inputLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
    
        inputField.setForeground(lightGray);
        inputField.setFont(new Font("Consolas", Font.PLAIN, 13));
        inputField.setBackground(new Color(50, 50, 60));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputField, BorderLayout.CENTER);
    
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(outputPanel, BorderLayout.CENTER);
        bottomPanel.add(inputPanel, BorderLayout.SOUTH);
    
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(visPanel, BorderLayout.CENTER);
        rightPanel.add(controlsPanel, BorderLayout.SOUTH);
    
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, codePanel, rightPanel);
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerSize(0); // Changed from 3 to 0 to remove the divider line
        splitPane.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOpaque(false);

        splitPane.setUI(new BasicSplitPaneUI() {
        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
            return new BasicSplitPaneDivider(this) {
                @Override
                public void paint(Graphics g) {
                    // Don't paint the divider at all
                }
            };
        }
    });

            
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    
        backgroundPanel.add(mainPanel, BorderLayout.CENTER);
        setContentPane(backgroundPanel);
    }
    
    private void addEventListeners() {
        // Visualize button
        visualizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visualizeCode();
            }
        });
        
        // Run button
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runCode();
            }
        });
        
        // First button
        firstButton.addActionListener(e -> {
            if (isVisualizing && !executionStates.isEmpty()) {
                currentStep = 0;
                updateVisualization();
                firstButton.setEnabled(false);
                previousButton.setEnabled(false);  // Disabled when at first step
                nextButton.setEnabled(executionStates.size() > 1);
                lastButton.setEnabled(executionStates.size() > 1);
            }
        });

        // Previous button
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isVisualizing && currentStep > 0) {
                    currentStep--;  // Move to the previous step
                    updateVisualization();  // Update UI to show previous state
                    // Update button states
                    firstButton.setEnabled(currentStep > 0);
                    previousButton.setEnabled(currentStep > 0);
                    nextButton.setEnabled(true);
                    lastButton.setEnabled(true);
                }
            }
        });
                
        // Next button
        nextButton.addActionListener(e -> {
            if (isVisualizing && currentStep < executionStates.size() - 1) {
                currentStep++;
                updateVisualization();
                firstButton.setEnabled(currentStep > 0);
                previousButton.setEnabled(true);  // Enable previous since we moved forward
                nextButton.setEnabled(currentStep < executionStates.size() - 1);
                lastButton.setEnabled(currentStep < executionStates.size() - 1);
            }
        });
        
        // Last button
        lastButton.addActionListener(e -> {
            if (isVisualizing && !executionStates.isEmpty()) {
                currentStep = executionStates.size() - 1;
                updateVisualization();
                firstButton.setEnabled(true);
                previousButton.setEnabled(true);  // Enable previous since we're at the end
                nextButton.setEnabled(false);
                lastButton.setEnabled(false);
            }
        });
        
        // Language change listener
        languageSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update syntax highlighting based on selected language
                String language = (String) languageSelector.getSelectedItem();
                ((SyntaxHighlighter) codeEditor.getStyledDocument()).setLanguage(language);
            }
        });
    }
    
    private void visualizeCode() {
        try {
            // Get the code from the editor
            String code = codeEditor.getText();
            if (code.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter some code first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get the selected language
            String language = (String) languageSelector.getSelectedItem();
            
            // Execute the code and get the execution states
            executionStates = codeExecutor.execute(code, language);
            
            // Check if we have any states
            if (executionStates.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No execution states were generated. Please check your code.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Store persistent object IDs
            for (ExecutionState state : executionStates) {
                for (HeapObject obj : state.getHeapObjects()) {
                    persistentObjectIds.add(obj.getId());
                }
            }
            
            // Update the visualization
            currentStep = 0;
            isVisualizing = true;
            updateVisualization();
            
            // Enable navigation buttons
            firstButton.setEnabled(false);
            previousButton.setEnabled(false);  // Disabled at first step
            nextButton.setEnabled(executionStates.size() > 1);
            lastButton.setEnabled(executionStates.size() > 1);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error executing code: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void updateVisualization() {
        if (executionStates.isEmpty()) {
            return;
        }
        
        // Get the current execution state
        ExecutionState state = executionStates.get(currentStep);
        
        // Update the step label
        stepLabel.setText("Step " + (currentStep + 1) + " of " + executionStates.size());
        
        // Update the code display with highlighting
        String code = state.getCode(); // Get the code from the execution state
        if (code != null && !code.trim().isEmpty()) {
            codeLines = code.split("\n");
            currentLine = state.getLineNumber();
            
            // Create a text area for displaying code with the current line highlighted
            codeEditor.setText(code);
            
            // Highlight the current line in the code editor
            try {
                // Clear previous highlights
                codeEditor.getHighlighter().removeAllHighlights();
                
                // Calculate the position of the current line
                int lineStart = 0;
                for (int i = 0; i < currentLine - 1 && i < codeLines.length; i++) {
                    lineStart += codeLines[i].length() + 1; // +1 for the newline character
                }
                
                // Make sure we don't go out of bounds
                if (currentLine > 0 && currentLine <= codeLines.length) {
                    int lineEnd = lineStart + codeLines[currentLine - 1].length();
                    
                    // Skip empty lines and lines with only brackets
                    String currentLineText = codeLines[currentLine - 1].trim();
                    if (!currentLineText.isEmpty() && !currentLineText.matches("\\s*[{}]\\s*")) {
                        // Highlight the current line
                        codeEditor.setCaretPosition(lineStart);
                        codeEditor.moveCaretPosition(lineEnd);
                        codeEditor.getCaret().setSelectionVisible(true);
                        
                        // Set background color for the current line
                        codeEditor.getHighlighter().addHighlight(lineStart, lineEnd, 
                            new DefaultHighlighter.DefaultHighlightPainter(HIGHLIGHT_COLOR));
                        
                        // Scroll to make the current line visible
                        JViewport viewport = ((JScrollPane) codeEditor.getParent().getParent()).getViewport();
                        try {
                            Rectangle rect = codeEditor.modelToView2D(lineStart).getBounds();
                            viewport.setViewPosition(new Point(0, Math.max(0, rect.y - 100)));
                        } catch (Exception ex) {
                            // Fallback for older Java versions
                            try {
                                @SuppressWarnings("deprecation")
                                Rectangle rect = codeEditor.modelToView(lineStart);
                                viewport.setViewPosition(new Point(0, Math.max(0, rect.y - 100)));
                            } catch (Exception ignored) {
                                // Ignore any errors
                            }
                        }
                    } else {
                        // If current line is empty or just brackets, move to the next meaningful line
                        moveToNextMeaningfulLine();
                        return; // updateVisualization will be called again by moveToNextMeaningfulLine
                    }
                }
            } catch (Exception e) {
                // Ignore any errors in highlighting
            }
        }
        
        // Update the output area
        outputArea.setText(state.getOutput());
        
        // Repaint the visualization panel
        visualizationPanel.repaint();
    }
    
    private void moveToNextMeaningfulLine() {
        // If we're at an empty line or a line with just brackets, move to the next step
        if (currentStep < executionStates.size() - 1) {
            currentStep++;
            updateVisualization();
        }
    }
    
    private void drawVisualization(Graphics2D g2d, ExecutionState state) {
        // The background is already set in paintComponent
        
        // Draw section titles with enhanced visibility
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font(LABEL_FONT.getName(), Font.BOLD, 18));
        
        // Add semi-transparent backgrounds behind section titles for better readability
        int titleHeight = 30;
        int titleWidth = 100;
        
        // Frames title background
        g2d.setColor(new Color(20, 20, 50, 180));
        g2d.fillRect(40, 100, titleWidth, titleHeight);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("Frames", 50, 120);
        
        // Objects title background
        g2d.setColor(new Color(20, 20, 50, 180));
        g2d.fillRect(390, 100, titleWidth, titleHeight);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("Objects", 400, 120);
        
        // Draw object creation section at the top left if there are objects created
        List<HeapObject> createdObjects = new ArrayList<>();
        if (currentStep > 0) {
            // Compare with previous state to find newly created objects
            ExecutionState prevState = executionStates.get(currentStep - 1);
            for (HeapObject obj : state.getHeapObjects()) {
                boolean isNew = true;
                for (HeapObject prevObj : prevState.getHeapObjects()) {
                    if (obj.getId() == prevObj.getId()) {
                        isNew = false;
                        break;
                    }
                }
                if (isNew) {
                    createdObjects.add(obj);
                }
            }
        } else {
            // First step - all objects are new
            createdObjects.addAll(state.getHeapObjects());
        }
        
        if (!createdObjects.isEmpty()) {
            drawObjectCreation(g2d, state, 20, 20);
        }
        
        // Add a separator line with better visibility
        g2d.setColor(new Color(200, 200, 240, 180));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(350, 100, 350, visualizationPanel.getHeight() - 10);
        
        // Draw frames
        drawFrames(g2d, state, 50, 140);
        
        // Draw heap objects
        drawObjects(g2d, state, 400, 140);
        
        // Draw reference arrows
        drawReferenceArrows(g2d, state);
    }
    
    private void drawObjectCreation(Graphics2D g2d, ExecutionState state, int x, int y) {
        // Get objects created in this step
        List<HeapObject> createdObjects = new ArrayList<>();
        if (currentStep > 0) {
            // Compare with previous state to find newly created objects
            ExecutionState prevState = executionStates.get(currentStep - 1);
            for (HeapObject obj : state.getHeapObjects()) {
                boolean isNew = true;
                for (HeapObject prevObj : prevState.getHeapObjects()) {
                    if (obj.getId() == prevObj.getId()) {
                        isNew = false;
                        break;
                    }
                }
                if (isNew) {
                    createdObjects.add(obj);
                }
            }
        } else {
            // First step - all objects are new
            createdObjects.addAll(state.getHeapObjects());
        }
        
        // Draw created objects
        if (!createdObjects.isEmpty()) {
            // Create a title for the created objects list with background
            g2d.setColor(new Color(20, 50, 80, 200));
            g2d.fillRect(x, y, 200, 25);
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font(LABEL_FONT.getName(), Font.BOLD, 14));
            g2d.drawString("Objects Created", x + 10, y + 18);
            
            // Create a small box for the created objects list
            int boxWidth = 200;
            int boxHeight = createdObjects.size() * 20 + 10;
            g2d.setColor(new Color(30, 70, 100, 200));
            g2d.fillRect(x, y + 25, boxWidth, boxHeight);
            g2d.setColor(OUTLINE_COLOR);
            g2d.drawRect(x, y + 25, boxWidth, boxHeight);
            
            // Draw the created objects
            int textY = y + 45;
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            for (HeapObject obj : createdObjects) {
                g2d.drawString("created " + obj.getType() + " " + obj.getId(), x + 10, textY);
                textY += 20;
            }
        }
    }
    
    private void drawFrames(Graphics2D g2d, ExecutionState state, int x, int y) {
        // Draw frames
        List<Frame> frames = state.getFrames();
        Map<Frame, Rectangle> framePositions = new HashMap<>();
        
        for (Frame frame : frames) {
            int frameHeight = Math.max(50, 30 + frame.getVariables().size() * 20);
            int frameWidth = 200; // Width similar to the second image
            
            // Store the frame's position for arrow drawing
            Rectangle frameRect = new Rectangle(x, y, frameWidth, frameHeight);
            framePositions.put(frame, frameRect);
            
            // Determine if this is the current frame (to highlight it)
            boolean isCurrentFrame = frame.getLineNumber() == currentLine;
            
            // Draw frame box with enhanced visibility
            g2d.setColor(isCurrentFrame ? FRAME_COLOR.brighter() : FRAME_COLOR);
            g2d.fillRect(x, y, frameWidth, frameHeight);
            g2d.setColor(OUTLINE_COLOR);
            g2d.drawRect(x, y, frameWidth, frameHeight);
            
            // Draw frame name and line number
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2d.drawString(frame.getName() + ":" + frame.getLineNumber(), x + 5, y + 15);
            
            // Draw variables
            g2d.setFont(VARIABLE_FONT);
            int varY = y + 35;
            for (Map.Entry<String, Value> entry : frame.getVariables().entrySet()) {
                String varName = entry.getKey();
                Value value = entry.getValue();
                
                // Draw variable name
                g2d.drawString(varName, x + 20, varY);
                
                // Draw value or reference with underline for references
                if (value.isReference()) {
                    String refText = String.valueOf(value.getReference());
                    g2d.drawString(refText, x + 100, varY);
                    
                    // Draw underline
                    int textWidth = g2d.getFontMetrics().stringWidth(refText);
                    g2d.drawLine(x + 100, varY + 2, x + 100 + textWidth, varY + 2);
                } else {
                    // Draw primitive value
                    g2d.drawString(value.getPrimitiveValue(), x + 100, varY);
                }
                
                varY += 20;
            }
            
            // Draw return value for non-global frames
            if (!frame.getName().equals("global") && !frame.getName().equals("main")) {
                g2d.setColor(ARROW_COLOR);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2d.drawString("Return", x + 5, y + frameHeight - 10);
                g2d.drawString("value", x + 5, y + frameHeight - 2);
                g2d.setColor(TEXT_COLOR);
                g2d.drawString("void", x + 50, y + frameHeight - 5);
            }
            
            y += frameHeight + 10; // Move down for the next frame
        }
        
        // Store frame positions for arrow drawing
        state.setFramePositions(framePositions);
    }
    
    private void drawObjects(Graphics2D g2d, ExecutionState state, int x, int y) {
        List<HeapObject> heapObjects = state.getHeapObjects();
        Map<HeapObject, Rectangle> objectPositions = new HashMap<>();
        
        // Draw persistent objects that are not in the current state
        if (!persistentObjectIds.isEmpty()) {
            for (Integer id : persistentObjectIds) {
                if (persistentObjectIds.contains(id) && 
                    !containsObjectWithId(heapObjects, id)) {
                    // Find the object from previous states
                    HeapObject persistentObj = null;
                    for (int i = 0; i < currentStep; i++) {
                        ExecutionState prevState = executionStates.get(i);
                        for (HeapObject obj : prevState.getHeapObjects()) {
                            if (obj.getId() == id) {
                                persistentObj = obj;
                                break;
                            }
                        }
                        if (persistentObj != null) break;
                    }
                    
                    if (persistentObj != null) {
                        // Draw this persistent object
                        drawObjectBox(g2d, persistentObj, x, y, objectPositions, state);
                        y += calculateObjectHeight(persistentObj) + 10; // Move down for the next object
                    }
                }
            }
        }
        
        // Draw current heap objects
        for (HeapObject obj : heapObjects) {
            // Draw the object
            drawObjectBox(g2d, obj, x, y, objectPositions, state);
            y += calculateObjectHeight(obj) + 10; // Move down for the next object
        }
        
        // Store object positions for arrow drawing
        state.setObjectPositions(objectPositions);
    }
    
    private void drawObjectBox(Graphics2D g2d, HeapObject obj, int x, int y, 
                              Map<HeapObject, Rectangle> objectPositions, ExecutionState state) {
        // Calculate object height based on properties
        int objHeight = calculateObjectHeight(obj);
        int objWidth = 150;
        
        // Store the object's position for arrow drawing
        Rectangle objRect = new Rectangle(x, y, objWidth, objHeight);
        objectPositions.put(obj, objRect);
        
        // Add to persistent objects set
        persistentObjectIds.add(obj.getId());
        
        // Draw object box with proper background
        g2d.setColor(OBJECT_COLOR);
        g2d.fillRect(x, y, objWidth, objHeight);
        g2d.setColor(OUTLINE_COLOR);
        g2d.drawRect(x, y, objWidth, objHeight);
        
        // Draw object type and ID
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.drawString(obj.getType() + " instance", x + 5, y + 15);
        
        // Draw object properties
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        int propY = y + 35;
        for (Map.Entry<String, Value> entry : obj.getProperties().entrySet()) {
            String propName = entry.getKey();
            Value propValue = entry.getValue();
            
            // Draw property name
            g2d.drawString(propName, x + 10, propY);
            
            // Draw connection point for references
            if (propValue.isReference()) {
                g2d.setColor(ARROW_COLOR);
                g2d.fillOval(x + objWidth - 10, propY - 4, 8, 8);
                g2d.setColor(TEXT_COLOR);
            }
            
            // Draw value or reference
            if (propValue.isReference()) {
                // For references, we'll draw arrows later
                g2d.drawString("→", x + 80, propY);
            } else {
                // Draw primitive value
                g2d.drawString(propValue.getPrimitiveValue(), x + 80, propY);
            }
            
            propY += 20;
        }
    }
    
    private int calculateObjectHeight(HeapObject obj) {
        return Math.max(50, 30 + obj.getProperties().size() * 20);
    }
    
    private boolean containsObjectWithId(List<HeapObject> objects, int id) {
        for (HeapObject obj : objects) {
            if (obj.getId() == id) {
                return true;
            }
        }
        return false;
    }
    
    private void drawReferenceArrows(Graphics2D g2d, ExecutionState state) {
        Map<Frame, Rectangle> framePositions = state.getFramePositions();
        Map<HeapObject, Rectangle> objectPositions = state.getObjectPositions();
        
        // Draw arrows from frames to objects
        for (Map.Entry<Frame, Rectangle> frameEntry : framePositions.entrySet()) {
            Frame frame = frameEntry.getKey();
            Rectangle frameRect = frameEntry.getValue();
            
            // Check each variable in the frame
            int varY = frameRect.y + 35;
            for (Map.Entry<String, Value> varEntry : frame.getVariables().entrySet()) {
                Value value = varEntry.getValue();
                
                // Draw reference arrow if this is a reference
                if (value.isReference()) {
                    int targetId = value.getReference();
                    HeapObject targetObj = findHeapObjectById(state, targetId);
                    if (targetObj != null) {
                        Rectangle objRect = objectPositions.get(targetObj);
                        if (objRect != null) {
                            // Draw arrow from variable to object
                            drawArrow(g2d, 
                                     frameRect.x + 100, varY, 
                                     objRect.x, objRect.y + 20);
                        }
                    }
                }
                
                varY += 20;
            }
        }
        
        // Draw arrows between objects
        for (Map.Entry<HeapObject, Rectangle> objEntry : objectPositions.entrySet()) {
            HeapObject obj = objEntry.getKey();
            Rectangle objRect = objEntry.getValue();
            
            // Check each property in the object
            int propY = objRect.y + 35;
            for (Map.Entry<String, Value> propEntry : obj.getProperties().entrySet()) {
                Value value = propEntry.getValue();
                
                // Draw reference arrow if this is a reference
                if (value.isReference()) {
                    int targetId = value.getReference();
                    HeapObject targetObj = findHeapObjectById(state, targetId);
                    if (targetObj != null && targetObj != obj) { // Avoid self-references
                        Rectangle targetRect = objectPositions.get(targetObj);
                        if (targetRect != null) {
                            // Draw arrow from property to target object
                            drawArrow(g2d, 
                                     objRect.x + objRect.width - 5, propY - 4, 
                                     targetRect.x, targetRect.y + 20);
                        }
                    }
                }
                
                propY += 20;
            }
        }
    }
    
    private HeapObject findHeapObjectById(ExecutionState state, int id) {
        for (HeapObject obj : state.getHeapObjects()) {
            if (obj.getId() == id) {
                return obj;
            }
        }
        return null;
    }
    
    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        // Draw arrow line
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(ARROW_COLOR);
        g2d.drawLine(x1, y1, x2, y2);
        
        // Calculate arrow head
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 8;
        int x3 = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
        int y3 = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
        int x4 = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
        int y4 = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));
        
        // Draw arrow head
        int[] xPoints = {x2, x3, x4};
        int[] yPoints = {y2, y3, y4};
        g2d.fillPolygon(xPoints, yPoints, 3);
    }
    
    private void runCode() {
        String code = codeEditor.getText();
        String language = (String) languageSelector.getSelectedItem();
        
        if (code.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter some code.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Clear the output area
            outputArea.setText("");
            
            // Create a temporary file to run the code
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            File tempFile;
            ProcessBuilder processBuilder;
            
            if ("Java".equals(language)) {
                // Check if code is a fragment or complete class
                boolean isFragment = !code.contains("public class");
                String className;
                String finalCode;
                
                if (isFragment) {
                    // Check if the code contains TestArray class definition
                    boolean hasTestArray = code.contains("class TestArray");
                    
                    // Wrap the code fragment in a class
                    className = "CodeFragment";
                    StringBuilder codeBuilder = new StringBuilder();
                    codeBuilder.append("import java.util.*; \n");
                    
                    // Add TestArray class if it's referenced but not defined
                    if (!hasTestArray && code.contains("TestArray")) {
                        codeBuilder.append("class TestArray {\n")
                                  .append("    int MAX(int[] Arry) {\n")
                                  .append("        int maxValue = Arry[0];\n")
                                  .append("        for(int i=1; i<Arry.length; i++) {\n")
                                  .append("            if(Arry[i] > maxValue) {\n")
                                  .append("                maxValue = Arry[i];\n")
                                  .append("            }\n")
                                  .append("        }\n")
                                  .append("        return maxValue;\n")
                                  .append("    }\n\n")
                                  .append("    int MIN(int[] Arry) {\n")
                                  .append("        int minValue = Arry[0];\n")
                                  .append("        for(int i=1; i<Arry.length; i++) {\n")
                                  .append("            if(Arry[i] < minValue) {\n")
                                  .append("                minValue = Arry[i];\n")
                                  .append("            }\n")
                                  .append("        }\n")
                                  .append("        return minValue;\n")
                                  .append("    }\n")
                                  .append("}\n\n");
                    }
                    
                    codeBuilder.append("public class ").append(className).append(" {\n")
                              .append("    public static void main(String[] args) {\n")
                              .append("        ").append(code.replace("\n", "\n        ")).append("\n")
                              .append("    }\n")
                              .append("}\n");
                    
                    finalCode = codeBuilder.toString();
                } else {
                    // Extract class name from code
                    className = extractClassName(code);
                    if (className == null) {
                        JOptionPane.showMessageDialog(this, "Could not find a valid class name in the Java code.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    finalCode = code;
                }
                
                // Create Java file
                tempFile = new File(tempDir, className + ".java");
                try (FileWriter writer = new FileWriter(tempFile)) {
                    writer.write(finalCode);
                }
                
                // Compile Java file
                processBuilder = new ProcessBuilder("javac", tempFile.getAbsolutePath());
                Process compileProcess = processBuilder.start();
                int compileResult = compileProcess.waitFor();
                
                if (compileResult != 0) {
                    // Compilation error
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            outputArea.append(line + "\n");
                        }
                    }
                    return;
                }
                
                // Run Java program
                processBuilder = new ProcessBuilder("java", "-cp", tempDir.getAbsolutePath(), className);
            } else { // Python
                // Create Python file
                tempFile = new File(tempDir, "program.py");
                try (FileWriter writer = new FileWriter(tempFile)) {
                    writer.write(code);
                }
                
                // Run Python program
                processBuilder = new ProcessBuilder("python", tempFile.getAbsolutePath());
            }
            
            // Set up process input/output
            processBuilder.redirectErrorStream(true); // Merge stderr into stdout
            final Process runProcess = processBuilder.start();
            
            // Clear any existing action listeners from the input field
            for (ActionListener al : inputField.getActionListeners()) {
                inputField.removeActionListener(al);
            }
            
            // Set up a PipedOutputStream to connect to the process
            PipedOutputStream outputStream = new PipedOutputStream();
            PipedInputStream inputStream = new PipedInputStream(outputStream);
            
            // Connect the input stream to the process
            Thread inputThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        runProcess.getOutputStream().write(buffer, 0, bytesRead);
                        runProcess.getOutputStream().flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            inputThread.start();
            
            // Create a thread to read process output
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String outputLine = line;
                        SwingUtilities.invokeLater(() -> {
                            outputArea.append(outputLine + "\n");
                            // Scroll to the bottom
                            outputArea.setCaretPosition(outputArea.getDocument().getLength());
                            // Focus on input field after output is shown
                            inputField.requestFocusInWindow();
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            outputThread.start();
            
            // Set up the input field
            inputField.setEnabled(true);
            inputField.requestFocusInWindow();
            
            // Add action listener for the input field
            inputField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String input = inputField.getText();
                        outputArea.append(input + "\n");
                        
                        // Send input to the process
                        outputStream.write((input + "\n").getBytes());
                        outputStream.flush();
                        
                        // Clear the input field
                        inputField.setText("");
                        inputField.requestFocusInWindow();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            inputStream.close();
            
        } catch (Exception ex) {
            outputArea.setText("Error executing code: " + ex.getMessage());
            ex.printStackTrace();
        }
    
    }
    
    private String extractClassName(String code) {
        // Simple regex to extract class name
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("class\\s+(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new CodeVisualizer().setVisible(true);
        });
    }

}

