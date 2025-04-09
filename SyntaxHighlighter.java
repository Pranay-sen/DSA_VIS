import javax.swing.text.*;
import javax.swing.*;
import java.awt.Color;
import java.util.regex.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SyntaxHighlighter extends DefaultStyledDocument {
    private static final Color KEYWORD_COLOR = new Color(127, 0, 85); // Purple
    private static final Color STRING_COLOR = new Color(42, 0, 255); // Blue
    private static final Color COMMENT_COLOR = new Color(0, 128, 0); // Green
    private static final Color NUMBER_COLOR = new Color(0, 0, 205); // Dark blue
    private static final Color DEFAULT_COLOR = Color.BLACK;
    
    private JTextPane textPane;
    private String language;
    
    // Styles
    private Style keywordStyle;
    private Style stringStyle;
    private Style commentStyle;
    private Style numberStyle;
    private Style defaultStyle;
    
    // Patterns
    private Pattern javaKeywordPattern;
    private Pattern pythonKeywordPattern;
    private Pattern javaStringPattern;
    private Pattern pythonStringPattern;
    private Pattern javaCommentPattern;
    private Pattern pythonCommentPattern;
    private Pattern numberPattern;
    
    public SyntaxHighlighter(JTextPane textPane) {
        this.textPane = textPane;
        this.textPane.setDocument(this);
        this.language = "Java"; // Default language
        
        // Initialize styles
        initStyles();
        
        // Initialize patterns
        initPatterns();
        
        // Add document listener
        this.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> highlightSyntax());
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> highlightSyntax());
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                // Plain text components don't fire these events
            }
        });
    }
    
    private void initStyles() {
        // Keyword style
        keywordStyle = textPane.addStyle("Keyword", null);
        StyleConstants.setForeground(keywordStyle, KEYWORD_COLOR);
        StyleConstants.setBold(keywordStyle, true);
        
        // String style
        stringStyle = textPane.addStyle("String", null);
        StyleConstants.setForeground(stringStyle, STRING_COLOR);
        
        // Comment style
        commentStyle = textPane.addStyle("Comment", null);
        StyleConstants.setForeground(commentStyle, COMMENT_COLOR);
        StyleConstants.setItalic(commentStyle, true);
        
        // Number style
        numberStyle = textPane.addStyle("Number", null);
        StyleConstants.setForeground(numberStyle, NUMBER_COLOR);
        
        // Default style
        defaultStyle = textPane.addStyle("Default", null);
        StyleConstants.setForeground(defaultStyle, DEFAULT_COLOR);
    }
    
    private void initPatterns() {
        // Java keywords
        String javaKeywords = "\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|" +
                "do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|" +
                "instanceof|int|interface|long|native|new|package|private|protected|public|" +
                "return|short|static|strictfp|super|switch|synchronized|this|throw|throws|" +
                "transient|try|void|volatile|while|true|false|null)\\b";
        javaKeywordPattern = Pattern.compile(javaKeywords);
        
        // Python keywords
        String pythonKeywords = "\\b(and|as|assert|break|class|continue|def|del|elif|else|except|" +
                "False|finally|for|from|global|if|import|in|is|lambda|None|nonlocal|not|" +
                "or|pass|raise|return|True|try|while|with|yield)\\b";
        pythonKeywordPattern = Pattern.compile(pythonKeywords);
        
        // String patterns
        javaStringPattern = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
        pythonStringPattern = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'|\"\"\"|'''");
        
        // Comment patterns
        javaCommentPattern = Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/");
        pythonCommentPattern = Pattern.compile("#.*");
        
        // Number pattern (works for both languages)
        numberPattern = Pattern.compile("\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b");
    }
    
    public void setLanguage(String language) {
        this.language = language;
        highlightSyntax();
    }
    
    private void highlightSyntax() {
        try {
            String text = textPane.getText();
            
            // Reset styles
            setCharacterAttributes(0, text.length(), defaultStyle, true);
            
            // Apply syntax highlighting based on language
            if ("Java".equals(language)) {
                highlightJavaSyntax(text);
            } else if ("Python".equals(language)) {
                highlightPythonSyntax(text);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void highlightJavaSyntax(String text) {
        // Highlight comments first (they take precedence)
        highlightPattern(text, javaCommentPattern, commentStyle);
        
        // Highlight strings
        highlightPattern(text, javaStringPattern, stringStyle);
        
        // Highlight keywords
        highlightPattern(text, javaKeywordPattern, keywordStyle);
        
        // Highlight numbers
        highlightPattern(text, numberPattern, numberStyle);
    }
    
    private void highlightPythonSyntax(String text) {
        // Highlight comments first
        highlightPattern(text, pythonCommentPattern, commentStyle);
        
        // Highlight strings
        highlightPattern(text, pythonStringPattern, stringStyle);
        
        // Highlight keywords
        highlightPattern(text, pythonKeywordPattern, keywordStyle);
        
        // Highlight numbers
        highlightPattern(text, numberPattern, numberStyle);
    }
    
    private void highlightPattern(String text, Pattern pattern, Style style) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), style, true);
        }
    }
    
    @Override
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offset, str, a);
    }
    
    @Override
    public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
    }
}
