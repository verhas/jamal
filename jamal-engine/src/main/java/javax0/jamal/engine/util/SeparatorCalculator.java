package javax0.jamal.engine.util;

/**
 * Calculates a string using the characters give in the argument to the constructor that does not appear in a given
 * string.
 */
public class SeparatorCalculator {

    final String chars;

    public SeparatorCalculator(String chars) {
        this.chars = chars;
        top = new Layer(chars.length());
    }

    private static final class Layer {
        final private Layer[] nodes;

        private Layer(int size) {
            nodes = new Layer[size];
        }
    }

    private final Layer top;
    private static final int N = 100;
    private Layer[] layers = new Layer[N];

    public String calculate(final String input) {
        int currentLevel = -1;
        for (final char ch : input.toCharArray()) {
            final int index = chars.indexOf(ch);
            if (index == -1) {
                currentLevel = -1;
            } else {
                currentLevel++;
                if (currentLevel == layers.length) {
                    resizeLayers();
                }
                layers[currentLevel] = top;
                for (int i = currentLevel; i >= 0; i--) {
                    if (layers[i].nodes[index] == null) {
                        layers[i].nodes[index] = new Layer(chars.length());
                    }
                    layers[i] = layers[i].nodes[index];
                }
            }
        }
        return getShort(top);
    }

    private String getShort(Layer layer) {
        for (int i = 0; i < layer.nodes.length; i++) {
            if (layer.nodes[i] == null) {
                return chars.substring(i, i + 1);
            }
        }
        int min = Integer.MAX_VALUE;
        String minString = "";
        int minIndex = 0;
        for (int i = 0; i < layer.nodes.length; i++) {
            final var s = getShort(layer.nodes[i]);
            if (s.length() < min) {
                min = s.length();
                minString = s;
                minIndex = i;
                if (min == 1) {
                    return chars.charAt(i) + minString;
                }
            }
        }
        return chars.charAt(minIndex) + minString;
    }

    private void resizeLayers() {
        Layer[] array = new Layer[layers.length + 2];
        System.arraycopy(layers, 0, array, 0, layers.length);
        layers = array;
    }

}
