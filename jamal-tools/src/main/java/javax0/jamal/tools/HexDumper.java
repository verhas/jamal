package javax0.jamal.tools;

/**
 * The {@code HexDumper} class provides a utility method to encode a byte array into a hexadecimal string representation.
 * It is useful for converting binary data into a human-readable hexadecimal format, often needed in debugging,
 * logging, or data serialization.
 */
public class HexDumper {

    /**
     * A lookup table for hexadecimal characters. The table contains the characters
     * '0'-'9' and 'a'-'f', corresponding to the hexadecimal digits.
     */
    private static final char[] LOOKUP_TABLE = new char[]{
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
            0x61, 0x62, 0x63, 0x64, 0x65, 0x66
    };

    /**
     * Encodes the given byte array into a hexadecimal string representation.
     *
     * <p>Each byte in the input array is represented by two hexadecimal characters.
     * The method processes each byte in the array, using the higher and lower 4 bits
     * to index into the lookup table to determine the corresponding hexadecimal characters.
     *
     * @param byteArray the byte array to be encoded into hexadecimal format
     * @return a string containing the hexadecimal representation of the input byte array
     * @throws NullPointerException if the input byte array is {@code null}
     */
    public static String encode(byte[] byteArray) {
        final char[] buffer = new char[byteArray.length * 2];
        for (int i = 0; i < byteArray.length; i++) {
            buffer[i << 1] = LOOKUP_TABLE[(byteArray[i] >> 4) & 0xF];
            buffer[(i << 1) + 1] = LOOKUP_TABLE[(byteArray[i] & 0xF)];
        }
        return String.valueOf(buffer);
    }
}
