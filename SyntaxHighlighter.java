import javax.swing.text.*;
import javax.swing.*;
import java.awt.Color;
import java.util.regex.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.HashMap;
import java.util.Map;

public class SyntaxHighlighter extends DefaultStyledDocument {
    // Color constants
    private static final Color KEYWORD_COLOR = new Color(0, 0, 255);
    private static final Color STRING_COLOR = new Color(163, 21, 21);
    private static final Color COMMENT_COLOR = new Color(0, 128, 0);
    private static final Color NUMBER_COLOR = new Color(255, 0, 255);
    private static final Color SYMBOL_COLOR = new Color(128, 0, 0);
    private static final Color OPERATOR_COLOR = new Color(0, 0, 128);
    private static final Color DEFAULT_COLOR = Color.BLACK;

    private final JTextPane textPane;
    private String language;
    private final Map<String, LanguagePatterns> languagePatternsMap;
    
    // Style objects
    private final Style keywordStyle;
    private final Style stringStyle;
    private final Style commentStyle;
    private final Style numberStyle;
    private final Style defaultStyle;
    private final Style symbolStyle;
    private final Style operatorStyle;

    public SyntaxHighlighter(JTextPane textPane) {
        this.textPane = textPane;
        this.textPane.setDocument(this);
        this.language = "Java";
        this.languagePatternsMap = new HashMap<>();
        
        // Initialize styles
        this.keywordStyle = createStyle("Keyword", KEYWORD_COLOR, true, false);
        this.stringStyle = createStyle("String", STRING_COLOR, false, false);
        this.commentStyle = createStyle("Comment", COMMENT_COLOR, false, true);
        this.numberStyle = createStyle("Number", NUMBER_COLOR, false, false);
        this.defaultStyle = createStyle("Default", DEFAULT_COLOR, false, false);
        this.symbolStyle = createStyle("Symbol", SYMBOL_COLOR, false, false);
        this.operatorStyle = createStyle("Operator", OPERATOR_COLOR, false, false);
        
        // Initialize language patterns
        initializeLanguagePatterns();
        
        // Add document listener
        addDocumentListener(new SyntaxDocumentListener());
    }
    
    private Style createStyle(String name, Color color, boolean bold, boolean italic) {
        Style style = textPane.addStyle(name, null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, bold);
        StyleConstants.setItalic(style, italic);
        return style;
    }
    
    private void initializeLanguagePatterns() {
<<<<<<< HEAD
        Pattern commonNumberPattern=Pattern.compile("\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b");
        Pattern commonSymbolPattern=Pattern.compile("[\\{\\}\\[\\]\\(\\)\\.,;:]");
        Pattern commonOperatorPattern=Pattern.compile("[+\\-*/=<>!&|%^~]+");
        
        String javaKeywords="\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while|true|false|null|var|yield|record|sealed|permits|non-sealed)\\b";
        Pattern javaKeywordPattern=Pattern.compile(javaKeywords);
        Pattern javaStringPattern=Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
        Pattern javaCommentPattern=Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/");
        languagePatternsMap.put("Java",new LanguagePatterns(javaKeywordPattern,javaStringPattern,javaCommentPattern,commonNumberPattern,commonSymbolPattern,commonOperatorPattern));
        
        String pythonKeywords="\\b(False|None|True|and|as|assert|async|await|break|class|continue|def|del|elif|else|except|finally|for|from|global|if|import|in|is|lambda|nonlocal|not|or|pass|raise|return|try|while|with|yield|match|case)\\b";
        Pattern pythonKeywordPattern=Pattern.compile(pythonKeywords);
        Pattern pythonStringPattern=Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'|\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''");
        Pattern pythonCommentPattern=Pattern.compile("#.*");
        languagePatternsMap.put("Python",new LanguagePatterns(pythonKeywordPattern,pythonStringPattern,pythonCommentPattern,commonNumberPattern,commonSymbolPattern,commonOperatorPattern));
        
        String cppKeywords="\\b(auto|break|case|char|const|continue|default|do|double|else|enum|extern|float|for|goto|if|inline|int|long|register|restrict|return|short|signed|sizeof|static|struct|switch|typedef|union|unsigned|void|volatile|while|_Alignas|_Alignof|_Atomic|_Bool|_Complex|_Generic|_Imaginary|_Noreturn|_Static_assert|_Thread_local|namespace|class|public|private|protected|new|delete|this|template|typename|virtual|override|friend|operator|mutable|explicit|constexpr|nullptr|using|try|catch|throw)\\b";
        Pattern cppKeywordPattern=Pattern.compile(cppKeywords);
        Pattern cppStringPattern=Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
        Pattern cppCommentPattern=Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/");
        languagePatternsMap.put("C++",new LanguagePatterns(cppKeywordPattern,cppStringPattern,cppCommentPattern,commonNumberPattern,commonSymbolPattern,commonOperatorPattern));
        languagePatternsMap.put("C",languagePatternsMap.get("C++"));
        
        String rubyKeywords="\\b(alias|and|begin|break|case|class|def|defined\\?|do|else|elsif|end|ensure|false|for|if|in|module|next|nil|not|or|redo|rescue|retry|return|self|super|then|true|undef|unless|until|when|while|yield)\\b";
        Pattern rubyKeywordPattern=Pattern.compile(rubyKeywords);
        Pattern rubyStringPattern=Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
        Pattern rubyCommentPattern=Pattern.compile("#.*");
        languagePatternsMap.put("Ruby",new LanguagePatterns(rubyKeywordPattern,rubyStringPattern,rubyCommentPattern,commonNumberPattern,commonSymbolPattern,commonOperatorPattern));
        
        String rustKeywords="\\b(as|break|const|continue|crate|else|enum|extern|false|fn|for|if|impl|in|let|loop|match|mod|move|mut|pub|ref|return|self|Self|static|struct|super|trait|true|type|unsafe|use|where|while|async|await|dyn)\\b";
        Pattern rustKeywordPattern=Pattern.compile(rustKeywords);
        Pattern rustStringPattern=Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
        Pattern rustCommentPattern=Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/");
        languagePatternsMap.put("Rust",new LanguagePatterns(rustKeywordPattern,rustStringPattern,rustCommentPattern,commonNumberPattern,commonSymbolPattern,commonOperatorPattern));
        
        String goKeywords="\\b(break|case|chan|const|continue|default|defer|else|fallthrough|for|func|go|goto|if|import|interface|map|package|range|return|select|struct|switch|type|var)\\b";
        Pattern goKeywordPattern=Pattern.compile(goKeywords);
        Pattern goStringPattern=Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
        Pattern goCommentPattern=Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/");
        languagePatternsMap.put("Go",new LanguagePatterns(goKeywordPattern,goStringPattern,goCommentPattern,commonNumberPattern,commonSymbolPattern,commonOperatorPattern));
        
        String jsKeywords="\\b(await|break|case|catch|class|const|continue|debugger|default|delete|do|else|enum|export|extends|false|finally|for|function|if|import|in|instanceof|let|new|null|return|super|switch|this|throw|true|try|typeof|var|void|while|with|yield)\\b";
        Pattern jsKeywordPattern=Pattern.compile(jsKeywords);
        Pattern jsStringPattern=Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'|`([^`\\\\]|\\\\.)*`");
        Pattern jsCommentPattern=Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/");
        languagePatternsMap.put("JavaScript",new LanguagePatterns(jsKeywordPattern,jsStringPattern,jsCommentPattern,commonNumberPattern,commonSymbolPattern,commonOperatorPattern));
        languagePatternsMap.put("Node.js",languagePatternsMap.get("JavaScript"));
        }
        
=======
        // Common patterns (numbers and symbols are common across languages)
        Pattern commonNumberPattern = Pattern.compile("\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b");
        Pattern commonSymbolPattern = Pattern.compile("[\\{\\}\\[\\]\\(\\)\\.,;:]");
        Pattern commonOperatorPattern = Pattern.compile("[+\\-*/=<>!&|%^~]+");
        
        // Java
        String javaKeywords = "\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|" +
                "do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|" +
                "instanceof|int|interface|long|native|new|package|private|protected|public|" +
                "return|short|static|strictfp|super|switch|synchronized|this|throw|throws|" +
                "transient|try|void|volatile|while|true|false|null|var|yield|record|sealed|permits|non-sealed)\\b";
        Pattern javaKeywordPattern = Pattern.compile(javaKeywords);
        Pattern javaStringPattern = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
        Pattern javaCommentPattern = Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/");
        languagePatternsMap.put("Java", new LanguagePatterns(
                javaKeywordPattern, javaStringPattern, javaCommentPattern,
                commonNumberPattern, commonSymbolPattern, commonOperatorPattern
        ));
        
        // Python
        String pythonKeywords = "\\b(False|None|True|and|as|assert|async|await|break|class|continue|def|del|elif|" +
                "else|except|finally|for|from|global|if|import|in|is|lambda|nonlocal|not|or|pass|" +
                "raise|return|try|while|with|yield|match|case)\\b";
        Pattern pythonKeywordPattern = Pattern.compile(pythonKeywords);
        Pattern pythonStringPattern = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'|\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''");
        Pattern pythonCommentPattern = Pattern.compile("#.*");
        languagePatternsMap.put("Python", new LanguagePatterns(
                pythonKeywordPattern, pythonStringPattern, pythonCommentPattern,
                commonNumberPattern, commonSymbolPattern, commonOperatorPattern
        ));
        
        // C/C++
        String cppKeywords = "\\b(auto|break|case|char|const|continue|default|do|double|else|enum|extern|float|" +
                "for|goto|if|inline|int|long|register|restrict|return|short|signed|sizeof|static|struct|switch|typedef|" +
                "union|unsigned|void|volatile|while|_Alignas|_Alignof|_Atomic|_Bool|_Complex|_Generic|_Imaginary|_Noreturn|" +
                "_Static_assert|_Thread_local|namespace|class|public|private|protected|new|delete|this|template|typename|" +
                "virtual|override|friend|operator|mutable|explicit|constexpr|nullptr|using|try|catch|throw)\\b";
        Pattern cppKeywordPattern = Pattern.compile(cppKeywords);
        Pattern cppStringPattern = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
        Pattern cppCommentPattern = Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/");
        languagePatternsMap.put("C++", new LanguagePatterns(
                cppKeywordPattern, cppStringPattern, cppCommentPattern,
                commonNumberPattern, commonSymbolPattern, commonOperatorPattern
        ));
        languagePatternsMap.put("C", languagePatternsMap.get("C++"));
        
        // JavaScript/Node.js
        String jsKeywords = "\\b(await|break|case|catch|class|const|continue|debugger|default|delete|do|" +
                "else|enum|export|extends|false|finally|for|function|if|import|in|instanceof|let|new|null|" +
                "return|super|switch|this|throw|true|try|typeof|var|void|while|with|yield)\\b";
        Pattern jsKeywordPattern = Pattern.compile(jsKeywords);
        Pattern jsStringPattern = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'|`([^`\\\\]|\\\\.)*`");
        Pattern jsCommentPattern = Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/");
        languagePatternsMap.put("JavaScript", new LanguagePatterns(
                jsKeywordPattern, jsStringPattern, jsCommentPattern,
                commonNumberPattern, commonSymbolPattern, commonOperatorPattern
        ));
        languagePatternsMap.put("Node.js", languagePatternsMap.get("JavaScript"));
    }
>>>>>>> 075c35528cb881a3537536e5403360b67ee83f2e
    
    public void setLanguage(String language) {
        if (languagePatternsMap.containsKey(language)) {
            this.language = language;
            highlightSyntax();
        } else {
            System.err.println("Unsupported language: " + language);
        }
    }
    
    private void highlightSyntax() {
        try {
            String text = getText(0, getLength());
            
            // Reset all text to default style first
            setCharacterAttributes(0, text.length(), defaultStyle, true);
            
            LanguagePatterns patterns = languagePatternsMap.get(language);
            if (patterns == null) return;
            
            // Apply highlighting for each pattern type
            highlightPattern(text, patterns.keywordPattern, keywordStyle);
            highlightPattern(text, patterns.stringPattern, stringStyle);
            highlightPattern(text, patterns.commentPattern, commentStyle);
            highlightPattern(text, patterns.numberPattern, numberStyle);
            highlightPattern(text, patterns.symbolPattern, symbolStyle);
            highlightPattern(text, patterns.operatorPattern, operatorStyle);
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private void highlightPattern(String text, Pattern pattern, Style style) {
        if (pattern == null) return;
        
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            
            // Skip empty matches
            if (start == end) continue;
            
            setCharacterAttributes(start, end - start, style, true);
        }
    }
    
    @Override
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offset, str, a);
        SwingUtilities.invokeLater(this::highlightSyntax);
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        SwingUtilities.invokeLater(this::highlightSyntax);
    }
    
    // Helper class to hold language patterns
    private static class LanguagePatterns {
        final Pattern keywordPattern;
        final Pattern stringPattern;
        final Pattern commentPattern;
        final Pattern numberPattern;
        final Pattern symbolPattern;
        final Pattern operatorPattern;
        
        LanguagePatterns(Pattern keywordPattern, Pattern stringPattern, Pattern commentPattern,
                       Pattern numberPattern, Pattern symbolPattern, Pattern operatorPattern) {
            this.keywordPattern = keywordPattern;
            this.stringPattern = stringPattern;
            this.commentPattern = commentPattern;
            this.numberPattern = numberPattern;
            this.symbolPattern = symbolPattern;
            this.operatorPattern = operatorPattern;
        }
    }
    
    // Document listener for syntax highlighting
    private class SyntaxDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            scheduleHighlighting();
        }
        
        @Override
        public void removeUpdate(DocumentEvent e) {
            scheduleHighlighting();
        }
        
        @Override
        public void changedUpdate(DocumentEvent e) {
            // Plain text components don't fire these events
        }
        
        private void scheduleHighlighting() {
            SwingUtilities.invokeLater(SyntaxHighlighter.this::highlightSyntax);
        }
    }
}