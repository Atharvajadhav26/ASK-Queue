package com.smartqueue.util;

/**
 * Generates sequential token numbers with a queue-specific prefix.
 * Example: OPD-101, OPD-102, BANK-001
 */
public final class TokenNumberGenerator {

    private TokenNumberGenerator() {
        // Utility class
    }

    /**
     * Generates a token number from prefix and sequence.
     * @param prefix Queue prefix (e.g., "OPD", "BANK")
     * @param sequence Current sequence number
     * @return Formatted token number (e.g., "OPD-101")
     */
    public static String generate(String prefix, int sequence) {
        if (prefix == null || prefix.isBlank()) {
            prefix = "Q";
        }
        // Start visible numbering from 100+ for professional appearance
        int displayNumber = 100 + sequence;
        return prefix.toUpperCase().trim() + "-" + displayNumber;
    }
}
