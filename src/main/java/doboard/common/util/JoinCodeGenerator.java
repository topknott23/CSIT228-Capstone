package doboard.common.util;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Utility class for generating unique, easy-to-type dorm join codes.
 * Codes are 6 characters long using alphanumeric characters (excluding ambiguous chars like 0/O, 1/I/L).
 */
public class JoinCodeGenerator {

    // Characters that are unambiguous and easy to type
    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final Random random = new SecureRandom();

    /**
     * Generates a random join code.
     *
     * @return A 6-character alphanumeric code (easy-to-type format)
     */
    public static String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return code.toString();
    }

    /**
     * Validates the format of a join code.
     *
     * @param code The code to validate
     * @return true if the code matches the expected format, false otherwise
     */
    public static boolean isValidFormat(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        // Code should be 6 characters of alphanumeric (from CHARS set)
        if (code.length() != CODE_LENGTH) {
            return false;
        }
        for (char c : code.toUpperCase().toCharArray()) {
            if (CHARS.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Normalizes a join code for database lookup (case-insensitive, trimmed).
     *
     * @param code The code to normalize
     * @return The normalized code in uppercase, or empty string if null
     */
    public static String normalize(String code) {
        if (code == null) {
            return "";
        }
        return code.trim().toUpperCase();
    }
}
