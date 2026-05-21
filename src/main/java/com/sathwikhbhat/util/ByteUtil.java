package com.sathwikhbhat.util;

import java.nio.ByteBuffer;

public class ByteUtil {
    private ByteUtil() {
        /* This utility class should not be instantiated */
    }

    public static byte[] longToBytes(long value) {
        return ByteBuffer
                .allocate(Long.BYTES)
                .putLong(value)
                .array();
    }

    public static long bytesToLong(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static byte[] intToBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }
}
