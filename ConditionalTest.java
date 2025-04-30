public class ConditionalTest {
    public static void main(String[] args) {
        int x = 10;
        int y = 5;
        
        System.out.println("Testing conditionals");
        
        if (x > y) {
            System.out.println("x is greater than y");
        } else {
            System.out.println("x is less than or equal to y");
        }
        
        if (x == 2 * y) {
            System.out.println("x is equal to 2 times y");
        } else {
            System.out.println("x is not equal to 2 times y");
        }
        
        System.out.println("Test complete");
    }
} 