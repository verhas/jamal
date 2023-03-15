package javax0.jamal.snippet;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.CollationKey;
import java.text.Collator;

/**
 * A general purpose collator that compares two strings, which are version numbers.
 * The versioning follows the recommendations of <a href="https://semver.org/">Semantic Versioning</a> with some laziness:
 * <p>
 * - The collation allows you to have more than 3 version numbers
 * - Version numbers may also be non-numeric just as pre-release tags in semver
 */
class SemVerCollator extends Collator {

    public SemVerCollator(){}

    @Override
    public int compare(final String source, final String target) {
        return getCollationKey(source).compareTo(getCollationKey(target));
    }

    @Override
    public CollationKey getCollationKey(final String source) {
        return new SemVerKey(source);
    }

    /**
     * There are no different instances of this collator, there is no state.
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * There are no different instances of this collator, there is no state.
     *
     * @param o the Collator to be compared with this.
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof SemVerCollator) {
            return true;
        } else {
            return false;
        }
    }

    static class SemVerKey extends CollationKey {
        private final String[] digits;
        private final String[] pre;

        /**
         * Arbitrary, but reasonable limit. This is the number of bytes that we use to store a numeric or alpha version
         * or pre-release tag part. The numerical digits are stored big endian with zero bytes padded at the start.
         * Each big integer byte is stored in two bytes, both 0-F value (upper half and then the lower half). This
         * ensures that the bytes of numeric version are always smaller than any non-numeric, which are simply stored
         * as their ascii code.
         * <p>
         * Non-numeric parts are stored as their ascii code padded with 0xff at the right.
         * <p>
         * The maximum size of a Long in Java 8 bytes, that is 16 half bytes. That is already a very big number.
         * This would make 16, 17 or something like that a safe bet. At the same time alpha parts can be long.
         * Probably 20 characters, THIS-IS-A-FAIRLY_LONG like this can be okay, but in extreme cases somebody can
         * run amok, so 80, which was a whole terminal character line in the age of vt100, should be fairly safe.
         * <p>
         * Note that the individual version tags have to be stored fixed length otherwise the byte array comparison may
         * not work.
         * <p>
         * To be honest, I do not know if there is any kind of sort or anything that uses this method.
         */
        private static final int DIGIT_SIZE = 80; // bytes

        @Override
        public byte[] toByteArray() {
            final var buffer = new ByteArrayOutputStream();
            writeDigits(buffer, digits);
            if (pre == null) {
                buffer.write(0xff);
            } else {
                buffer.write(0xfe);
                writeDigits(buffer, pre);
            }
            return buffer.toByteArray();
        }

        private void writeDigits(final ByteArrayOutputStream buffer, final String[] digits) {
            for (int i = 0; i < digits.length; i++) {
                final var bigi = toBigInteger(digits[i]);
                if (bigi != null) {
                    final var bytes = bigi.toByteArray();
                    for (int k = bytes.length; k < DIGIT_SIZE; k++) {
                        buffer.write(0);
                    }
                    for (int j = 0; j < bytes.length; j++) {
                        buffer.write(((int) bytes[j]) >>> 4);
                        buffer.write(bytes[j] & 0x0f);
                    }
                } else {
                    buffer.writeBytes(digits[i].getBytes(StandardCharsets.UTF_8));
                    for (int j = digits[i].length(); j < DIGIT_SIZE; j++) {
                        buffer.write(0xff);
                    }
                }
                buffer.write(0xff);
            }
        }

        SemVerKey(final String version) {
            super(version);
            final var dash = version.indexOf('-');
            final int build = version.indexOf('+');
            if (dash != -1 && (build == -1 || dash < build)) {
                //SemVer 11/1 "Build metadata does not figure into precedence"
                if (build == -1) {
                    pre = version.substring(dash + 1).split("\\.", -1);
                } else {
                    pre = version.substring(dash + 1, build).split("\\.", -1);
                }
            } else {
                pre = null;
            }
            if (build == -1 && dash == -1) {
                digits = version.split("\\.", -1);
            } else {
                final var versionEnd = dash == -1 ? build : dash;
                digits = version.substring(0, versionEnd).split("\\.", -1);
            }
        }

        /**
         * Compare two digits. If they are both numeric then they are converted to big decimal and compared.
         * If any of them is not convertable to BigDecimal, then they are compared as strings.
         *
         * @param a the first digit
         * @param b the second digit
         * @return -1 if a < b, +1 if b < a and 0 if a == b
         */
        int compareDigits(final String a, final String b) {
            final var la = toBigInteger(a);
            final var lb = toBigInteger(b);
            // numeric always precedes non-numeric, SemVer p11/4/3
            if (la != null && lb == null) {
                return -1;
            }
            // numeric always precedes non-numeric, SemVer p11/4/3
            if (la == null && lb != null) {
                return +1;
            }
            // ascii collating order, SemVer 11/4/2
            //noinspection ConstantValue
            if (la == null && lb == null) {
                return a.compareTo(b);
            } else {
                // numeric comparison, SemVer 11/2, 11/4/2
                return la.compareTo(lb);
            }
        }

        /**
         * Convert a string to a BigInteger or return null
         *
         * @param number the number as string
         * @return the BigInteger object or null
         */
        private static BigInteger toBigInteger(final String number) {
            try {
                return new BigInteger(number);
            } catch (NumberFormatException __) {
                return null;
            }
        }

        /**
         * Compare the two arrays of version digits.
         * This is a separate method used to compare both the version numbers and the pre-release tag parts.
         *
         * @param digitsA the first digits array
         * @param digitsB the second digits array
         * @return -1 if a < b, +1 if b < a and 0 if a == b
         */
        private int compare(final String[] digitsA, final String[] digitsB) {
            int i = 0;
            while (i < digitsA.length && i < digitsB.length) {
                final var res = compareDigits(digitsA[i], digitsB[i]);
                if (res != 0) {
                    return res;
                }
                i++;
            }
            if (digitsA.length < digitsB.length) {
                return -1;
            }
            if (digitsB.length < digitsA.length) {
                return +1;
            }
            return 0;
        }

        /**
         * Compare two version keys. If the other key is not a {@code SemVerKey} then a runtime exception will be thrown
         *
         * @param otherCK the object to be compared. Check and cast to {@code SemVerKey} at the start and not used anymore.
         * @return -1 if other < this, +1 if this < other and 0 if this == other
         */
        @Override
        public int compareTo(final CollationKey otherCK) {
            if (otherCK instanceof SemVerKey) {
                final var other = (SemVerKey) otherCK;
                final var res = compare(digits, other.digits);
                if (res != 0) {
                    return res;
                }
                if (pre == null && other.pre != null) {
                    return +1;
                }
                if (pre != null && other.pre == null) {
                    return -1;
                }
                if (pre == null && other.pre == null) {
                    return 0;
                }
                return compare(pre, other.pre);
            } else {
                throw new RuntimeException("Sorting tries to compare a version to a not version. This is probably an internal error.");
            }
        }
    }

}
