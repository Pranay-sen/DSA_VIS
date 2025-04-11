import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;

/**
 * Component that displays line numbers for a JTextComponent
 */
public class LineNumberComponent extends JPanel {
    private static final int MARGIN = 5;
    private final JTextComponent textComponent;
    private final FontMetrics fontMetrics;
    
    public LineNumberComponent(JTextComponent textComponent) {
        this.textComponent = textComponent;
        Font font = new Font(Font.MONOSPACED, Font.BOLD, 14);
        setFont(font);
        fontMetrics = getFontMetrics(font);
        
        setBackground(new Color(240, 240, 240));
        setForeground(Color.GRAY);
        
        // Add a document listener to the text component
        textComponent.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                repaint();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                repaint();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                repaint();
            }
        });
        
        // Set minimum width
        int width = fontMetrics.stringWidth("999") + MARGIN * 2;
        setPreferredSize(new Dimension(width, 0));
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Enable anti-aliasing for smoother text
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Get the clip bounds
        Rectangle clip = g.getClipBounds();
        
        // Get the height of each line
        int lineHeight = fontMetrics.getHeight();
        
        // Calculate the starting and ending lines to draw
        int startLine = clip.y / lineHeight + 1;
        int endLine = (clip.y + clip.height) / lineHeight + 1;
        
        // Get the total number of lines in the document
        int totalLines = getLineCount();
        endLine = Math.min(endLine, totalLines);
        
        // Draw each visible line number
        for (int i = startLine; i <= endLine; i++) {
            String lineNumber = String.valueOf(i);
            int stringWidth = fontMetrics.stringWidth(lineNumber);
            int x = getWidth() - stringWidth - MARGIN;
            int y = (i - 1) * lineHeight + fontMetrics.getAscent();
            
            g.drawString(lineNumber, x, y);
        }
    }
    
    @Override
    public Dimension getPreferredSize() {
        // Calculate the width based on the number of digits in the maximum line number
        int lineCount = getLineCount();
        int digits = String.valueOf(lineCount).length();
        int width = fontMetrics.stringWidth("0".repeat(digits)) + MARGIN * 2;
        
        return new Dimension(width, textComponent.getHeight());
    }
    
    private int getLineCount() {
        // Count the number of lines in the text component
        String text = textComponent.getText();
        if (text == null || text.isEmpty()) {
            return 1;
        }
        
        int count = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }
}
