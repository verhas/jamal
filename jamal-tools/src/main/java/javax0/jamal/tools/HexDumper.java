package javax0.jamal.tools;

public class HexDumper {
    private static final char[] LOOKUP_TABLE = new char[]{0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66};

    public static String encode(byte[] byteArray) {
        final char[] buffer = new char[byteArray.length * 2];
        for (int i = 0; i < byteArray.length; i++) {
            buffer[i << 1] = LOOKUP_TABLE[(byteArray[i] >> 4) & 0xF];
            buffer[(i << 1) + 1] = LOOKUP_TABLE[(byteArray[i] & 0xF)];
        }
        return String.valueOf(buffer);
    }
}
