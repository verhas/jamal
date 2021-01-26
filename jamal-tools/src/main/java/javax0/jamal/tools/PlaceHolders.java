package javax0.jamal.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Replace the placeholders to the actual values in strings. The placeholders are searched from left to right in the
 * string and are replaced one after the other. The string that gets into the plae of the placeholder will not be
 * searched for additional placeholders.
 * <p>
 * Placeholders are specified as strings and the replacement can be specified either as  string or as a string supplier.
 * The supplier may throw an exception.
 */
public class PlaceHolders {

    /**
     * The simple implementation of the placeholder functionality that does not handle suppliers. This implementation
     * formally is the extension of the more complex one because and not the other way around. The whole hocus pocus
     * with the class extensions is to let two different {@link Holder#format(String)} replace()} methods. One that
     * throws exception and the other one that does not.
     *
     * @param k1 the key of the placeholder
     * @param v1 the value of the placeholder
     * @return a placeholder implementation
     */
    public static Holder of(
        String k1,
        String v1
    ) {
        return new Holder(List.of(k1, v1));
    }

    //<editor-fold desc="multi argument versions">

    public static Holder of() {
        return new Holder(List.of());
    }

    public static Holder of(
        String k1,
        String v1,
        String k2,
        String v2
    ) {
        return new Holder(List.of(k1, v1, k2, v2));
    }

    public static Holder of(
        String k1,
        String v1,
        String k2,
        String v2,
        String k3,
        String v3
    ) {
        return new Holder(List.of(k1, v1, k2, v2, k3, v3));
    }

    public static Holder of(
        String k1,
        String v1,
        String k2,
        String v2,
        String k3,
        String v3,
        String k4,
        String v4
    ) {
        return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    public static Holder of(
        String k1,
        String v1,
        String k2,
        String v2,
        String k3,
        String v3,
        String k4,
        String v4,
        String k5,
        String v5
    ) {
        return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
    }

    public static Holder of(
        String k1,
        String v1,
        String k2,
        String v2,
        String k3,
        String v3,
        String k4,
        String v4,
        String k5,
        String v5,
        String k6,
        String v6
    ) {
        return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6));
    }

    public static Holder of(
        String k1,
        String v1,
        String k2,
        String v2,
        String k3,
        String v3,
        String k4,
        String v4,
        String k5,
        String v5,
        String k6,
        String v6,
        String k7,
        String v7
    ) {
        return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7));
    }

    public static Holder of(
        String k1,
        String v1,
        String k2,
        String v2,
        String k3,
        String v3,
        String k4,
        String v4,
        String k5,
        String v5,
        String k6,
        String v6,
        String k7,
        String v7,
        String k8,
        String v8
    ) {
        return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8));
    }

    public static Holder of(
        String k1,
        String v1,
        String k2,
        String v2,
        String k3,
        String v3,
        String k4,
        String v4,
        String k5,
        String v5,
        String k6,
        String v6,
        String k7,
        String v7,
        String k8,
        String v8,
        String k9,
        String v9
    ) {
        return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9));
    }

    public static Holder of(
        String k1,
        String v1,
        String k2,
        String v2,
        String k3,
        String v3,
        String k4,
        String v4,
        String k5,
        String v5,
        String k6,
        String v6,
        String k7,
        String v7,
        String k8,
        String v8,
        String k9,
        String v9,
        String k10,
        String v10
    ) {
        return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10));
    }

    public static Holder of(
        String k1,
        String v1,
        String k2,
        String v2,
        String k3,
        String v3,
        String k4,
        String v4,
        String k5,
        String v5,
        String k6,
        String v6,
        String k7,
        String v7,
        String k8,
        String v8,
        String k9,
        String v9,
        String k10,
        String v10,
        String k11,
        String v11
    ) {
        return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10, k11, v11));
    }
    //</editor-fold>

    public static class Holder extends ComplexHolder {

        private Holder(List<String> values) {
            super(values);
        }

        public ComplexHolder and(final Map<String, ThrowingStringSupplier> values) {
            return new ComplexHolder(super.computedValues).and(values);
        }

        //<editor-fold desc="multi argument versions">
        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2
        ) {
            return new ComplexHolder(super.computedValues).and(k2, v2);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3
        ) {
            return new ComplexHolder(super.computedValues).and(k2, v2, k3, v3);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4
        ) {
            return new ComplexHolder(super.computedValues).and(k2, v2, k3, v3, k4, v4);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5
        ) {
            return new ComplexHolder(super.computedValues).and(k2, v2, k3, v3, k4, v4, k5, v5);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5,
            String k6,
            ThrowingStringSupplier v6
        ) {
            return new ComplexHolder(super.computedValues).and(k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5,
            String k6,
            ThrowingStringSupplier v6,
            String k7,
            ThrowingStringSupplier v7
        ) {
            return new ComplexHolder(super.computedValues).and(k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5,
            String k6,
            ThrowingStringSupplier v6,
            String k7,
            ThrowingStringSupplier v7,
            String k8,
            ThrowingStringSupplier v8
        ) {
            return new ComplexHolder(super.computedValues).and(k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5,
            String k6,
            ThrowingStringSupplier v6,
            String k7,
            ThrowingStringSupplier v7,
            String k8,
            ThrowingStringSupplier v8,
            String k9,
            ThrowingStringSupplier v9
        ) {
            return new ComplexHolder(super.computedValues).and(k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5,
            String k6,
            ThrowingStringSupplier v6,
            String k7,
            ThrowingStringSupplier v7,
            String k8,
            ThrowingStringSupplier v8,
            String k9,
            ThrowingStringSupplier v9,
            String k10,
            ThrowingStringSupplier v10
        ) {
            return new ComplexHolder(super.computedValues).and(k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5,
            String k6,
            ThrowingStringSupplier v6,
            String k7,
            ThrowingStringSupplier v7,
            String k8,
            ThrowingStringSupplier v8,
            String k9,
            ThrowingStringSupplier v9,
            String k10,
            ThrowingStringSupplier v10,
            String k11,
            ThrowingStringSupplier v11
        ) {
            return new ComplexHolder(super.computedValues).and(k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10, k11, v11);
        }
        //</editor-fold>

        @Override
        public String format(final String original) {
            try {
                return super.format(original);
            } catch (Exception e) {
                throw new RuntimeException("Internal error in placeholder. Should never happen.");
            }
        }

        @Override
        String getValue(String key) {
            return super.computedValues.get(key);
        }


        //<editor-fold desc="non-static of() methods">
        public Holder of(
            String k1,
            String v1
        ) {
            return new Holder(List.of(k1, v1));
        }

        public Holder of(
            String k1,
            String v1,
            String k2,
            String v2
        ) {
            return new Holder(List.of(k1, v1, k2, v2));
        }

        public Holder of(
            String k1,
            String v1,
            String k2,
            String v2,
            String k3,
            String v3
        ) {
            return new Holder(List.of(k1, v1, k2, v2, k3, v3));
        }

        public Holder of(
            String k1,
            String v1,
            String k2,
            String v2,
            String k3,
            String v3,
            String k4,
            String v4
        ) {
            return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4));
        }

        public Holder of(
            String k1,
            String v1,
            String k2,
            String v2,
            String k3,
            String v3,
            String k4,
            String v4,
            String k5,
            String v5
        ) {
            return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
        }

        public Holder of(
            String k1,
            String v1,
            String k2,
            String v2,
            String k3,
            String v3,
            String k4,
            String v4,
            String k5,
            String v5,
            String k6,
            String v6
        ) {
            return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6));
        }

        public Holder of(
            String k1,
            String v1,
            String k2,
            String v2,
            String k3,
            String v3,
            String k4,
            String v4,
            String k5,
            String v5,
            String k6,
            String v6,
            String k7,
            String v7
        ) {
            return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7));
        }

        public Holder of(
            String k1,
            String v1,
            String k2,
            String v2,
            String k3,
            String v3,
            String k4,
            String v4,
            String k5,
            String v5,
            String k6,
            String v6,
            String k7,
            String v7,
            String k8,
            String v8
        ) {
            return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8));
        }

        public Holder of(
            String k1,
            String v1,
            String k2,
            String v2,
            String k3,
            String v3,
            String k4,
            String v4,
            String k5,
            String v5,
            String k6,
            String v6,
            String k7,
            String v7,
            String k8,
            String v8,
            String k9,
            String v9
        ) {
            return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9));
        }

        public Holder of(
            String k1,
            String v1,
            String k2,
            String v2,
            String k3,
            String v3,
            String k4,
            String v4,
            String k5,
            String v5,
            String k6,
            String v6,
            String k7,
            String v7,
            String k8,
            String v8,
            String k9,
            String v9,
            String k10,
            String v10
        ) {
            return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10));
        }

        public Holder of(
            String k1,
            String v1,
            String k2,
            String v2,
            String k3,
            String v3,
            String k4,
            String v4,
            String k5,
            String v5,
            String k6,
            String v6,
            String k7,
            String v7,
            String k8,
            String v8,
            String k9,
            String v9,
            String k10,
            String v10,
            String k11,
            String v11
        ) {
            return new Holder(List.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10, k11, v11));
        }
        //</editor-fold>

    }

    public static class ComplexHolder {

        private final Map<String, ThrowingStringSupplier> values = new HashMap<>();
        private final Map<String, String> computedValues = new HashMap<>();
        private final Set<String> keys = new HashSet<>();

        /**
         * Initialize the map with strings.
         *
         * @param values the map of placeholder to actual value strings
         */
        private ComplexHolder(final Map<String, String> values) {
            this.computedValues.putAll(values);
            keys.addAll(values.keySet());
        }

        private ComplexHolder(List<String> values) {
            for (int i = 0; i < values.size() - 1; i += 2) {
                this.computedValues.put(values.get(i), values.get(i + 1));
                keys.add(values.get(i));
            }
        }

        public interface ThrowingStringSupplier {
            String get() throws Exception;
        }

        public ComplexHolder and(final Map<String, ThrowingStringSupplier> values) {
            this.values.putAll(values);
            this.keys.addAll(values.keySet());
            return this;
        }

        public ComplexHolder and(final String k1, ThrowingStringSupplier v1) {
            if (computedValues.containsKey(k1)) {
                throw new IllegalArgumentException("The key '"+k1+"' shadows a key with precomputed value.");
            }
            return and(Map.of(k1, v1));
        }

        //<editor-fold desc="multi argument versions">
        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2
        ) {
            return and(k1, v1).and(k2, v2);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3
        ) {
            return and(k1, v1).and(k2, v2, k3, v3);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4
        ) {
            return and(k1, v1).and(k2, v2, k3, v3, k4, v4);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5
        ) {
            return and(k1, v1).and(k2, v2, k3, v3, k4, v4, k5, v5);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5,
            String k6,
            ThrowingStringSupplier v6
        ) {
            return and(k1, v1).and(k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5,
            String k6,
            ThrowingStringSupplier v6,
            String k7,
            ThrowingStringSupplier v7
        ) {
            return and(k1, v1).and(k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5,
            String k6,
            ThrowingStringSupplier v6,
            String k7,
            ThrowingStringSupplier v7,
            String k8,
            ThrowingStringSupplier v8
        ) {
            return and(k1, v1).and(k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5,
            String k6,
            ThrowingStringSupplier v6,
            String k7,
            ThrowingStringSupplier v7,
            String k8,
            ThrowingStringSupplier v8,
            String k9,
            ThrowingStringSupplier v9
        ) {
            return and(k1, v1).and(k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5,
            String k6,
            ThrowingStringSupplier v6,
            String k7,
            ThrowingStringSupplier v7,
            String k8,
            ThrowingStringSupplier v8,
            String k9,
            ThrowingStringSupplier v9,
            String k10,
            ThrowingStringSupplier v10
        ) {
            return and(k1, v1).and(k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
        }

        public ComplexHolder and(
            String k1,
            ThrowingStringSupplier v1,
            String k2,
            ThrowingStringSupplier v2,
            String k3,
            ThrowingStringSupplier v3,
            String k4,
            ThrowingStringSupplier v4,
            String k5,
            ThrowingStringSupplier v5,
            String k6,
            ThrowingStringSupplier v6,
            String k7,
            ThrowingStringSupplier v7,
            String k8,
            ThrowingStringSupplier v8,
            String k9,
            ThrowingStringSupplier v9,
            String k10,
            ThrowingStringSupplier v10,
            String k11,
            ThrowingStringSupplier v11
        ) {
            return and(k1, v1).and(k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10, k11, v11);
        }

        //</editor-fold>

        /**
         * Replace the placeholders in the string {@code format} with the values from the maps and return the result.
         * <p>
         * The placeholders and the values are stored in two maps. The keys in the maps are the placeholder strings and
         * the values are used to get the string to replace the placeholders with.
         * <p>
         * One map contains strings as values. The other one contains string suppliers. These suppliers are not {@link
         * java.util.function.Supplier}s because they may throw an exception. Any of the suppliers are invoked only if
         * there is a need for them. This is an important features, because it means that there will be no exception in
         * case the placeholder to be replaced by the result of the supplier is not used even if the the supplier would
         * throw an exception.
         * <p>
         * Any of the suppliers are invoked at most once, when they are referenced the first time. After that the result
         * is stored in the string value map and used from there. Consecutive invocation of {@code format()} will use
         * the already calculated value.
         * <p>
         * Placeholder replacement starts from the left to the right and replacement texts are not scanned for
         * placeholders. For example:
         *
         * <pre>{@code
         *   PlaceHolders.of("$a", "$b", "$b", "$a").format("$a $b")
         * }</pre>
         * <p>
         * will become
         *
         * <pre>{@code
         * "$b $a"
         * }</pre>
         *
         * @param original
         * @return
         * @throws Exception
         */
        public String format(final String original) throws Exception {
            StringBuilder sb = new StringBuilder(original);
            boolean repeatMore = true;
            int doneIndex = 0;
            while (repeatMore) {
                repeatMore = false;
                int min = Integer.MAX_VALUE;
                String firstKey = "";
                for (final var k : keys) {
                    int pos = sb.indexOf(k, doneIndex);
                    if (pos != -1 && pos < min) {
                        min = pos;
                        firstKey = k;
                    }
                }
                final var key = firstKey;
                if (min < sb.length()) {
                    final var value = getValue(key);
                    sb.replace(min, min + key.length(), value);
                    repeatMore = true;
                    doneIndex = min + value.length();
                }
            }
            return sb.toString();
        }

        String getValue(String key) throws Exception {
            if (!computedValues.containsKey(key)) {
                computedValues.put(key, values.get(key).get());
            }
            return computedValues.get(key);
        }
    }
}
