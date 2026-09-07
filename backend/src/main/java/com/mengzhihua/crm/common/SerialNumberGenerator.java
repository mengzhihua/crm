package com.mengzhihua.crm.common;

public final class SerialNumberGenerator {
    private SerialNumberGenerator() {
    }

    public static String next(String prefix, String latestNumber) {
        int sequence = 1;
        if (latestNumber != null && latestNumber.startsWith(prefix)) {
            String suffix = latestNumber.substring(prefix.length());
            if (!suffix.isEmpty()) {
                sequence = Integer.parseInt(suffix) + 1;
            }
        }
        return prefix + String.format("%04d", sequence);
    }
}
