package com.smartqueue.util;

import java.security.SecureRandom;

/**
 * Generates unique queue IDs in the format Q-XXXXXX
 * where X is an alphanumeric character.
 */
public final class QueueIdGenerator {

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int ID_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private QueueIdGenerator() {
        // Utility class
    }

    /**
     * Generates a unique queue ID like Q-8F42A1
     */
    public static String generate() {
        StringBuilder sb = new StringBuilder("Q-");
        for (int i = 0; i < ID_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
