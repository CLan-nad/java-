
import java.util.Random;

public class AESusing {

    public static class KeyGenerator {

        static String generateRandomKey() {
            Random random = new Random();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < 32; i++) {
                
                String hexDigit = Integer.toHexString(random.nextInt(16));
                sb.append(hexDigit);
            }

            return sb.toString();
        }

    }
} 