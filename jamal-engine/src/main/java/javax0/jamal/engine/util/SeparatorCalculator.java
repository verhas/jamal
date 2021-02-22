package javax0.jamal.engine.util;

import static java.lang.Math.min;

/**
 * Calculates a string using the characters given in the argument to the constructor that does not appear in a given
 * string.
 */
public class SeparatorCalculator {

    final String chars;

    /**
     * Create a separator calculator that will use only the characters that are given in {@code chars} to create the
     * separator string when {@link #calculate(String)} is invoked.
     *
     * @param chars the characters that can be used in the separator strings
     */
    public SeparatorCalculator(String chars) {
        this.chars = chars;
        top = new Layer(chars.length());
    }

    /**
     * One layer in the letter trie data structure. Each layer contains an array of the next layers. The array is
     * indexed by the character position in the String field {@code chars}. If the value of the array element is {@code
     * null} then the character was not found in the string with those characters before it as indicated by the upper
     * trie leaves.
     */
    private static final class Layer {
        final private Layer[] nodes;

        private Layer(int size) {
            nodes = new Layer[size];
        }
    }

    private final Layer top; // the root node of the trie
    private static final int N = 100; // the initial maximal depth of the trie
    private static final int INCREMENT = 10; // the initial maximal depth of the trie
    /**
     * This array references endpoints in the trie during the trie build-up. When we traverse along the characters at
     * the input we want to have a trie that matches any substring of the input. The element [currentLevel] references
     * the layer that is the path path matching from one character, the one before the current. The element
     * [currentLevel-1] references the layer that is the path matching two characters, the previous tro before the
     * current. And so on.
     * <p>
     * When we step forward we always open a new level unless there is a character that is not in the set of the
     * possible separator characters. At that point the structure can start from zero.
     */
    private Layer[] layers;

    /**
     * Calculate a separator string that can be used to signal the end of the input string. The separator string should
     * not appear inside the input and should contain only the characters that are in the character set that was passed
     * to the constructor of the class.
     *
     * @param input the string for which we need to calculate the separator string
     * @return the separator string, that is as short as possible and the input does not contain it
     */
    public String calculate(final String input) {
        layers = new Layer[min(N,input.length())];
        buildUpTrieFromInput(input);
        return getShort(top);
    }

    private void buildUpTrieFromInput(String input) {
        int currentLevel = -1;
        for (final char ch : input.toCharArray()) {
            final int index = chars.indexOf(ch);
            if (index == -1) {
                currentLevel = -1;
            } else {
                currentLevel = safelyIncreaseCurrentLevel(currentLevel);
                layers[currentLevel] = top;
                appendInAllLayers(currentLevel, index);
            }
        }
    }

    /**
     * Increase the value {@code currentLevel} to {@code currentLevel+1} and ensure that
     *
     * @param currentLevel
     * @return
     */
    private int safelyIncreaseCurrentLevel(int currentLevel) {
        currentLevel++;
        if (currentLevel == layers.length) {
            resizeLayers();
        }
        return currentLevel;
    }

    /**
     * Append the current character to each leaf of the tries that represent an endpoint starting with the letter one,
     * two, ... characters before the current one. The layer elements are referring to these leaves. After the character
     * added the new leaf will become the new layer for each and every layer.
     *
     * @param currentLevel the current deepest level where we are in the trie
     * @param index        the index value of the current character
     */
    private void appendInAllLayers(int currentLevel, int index) {
        for (int i = currentLevel; i >= 0; i--) {
            if (layers[i].nodes[index] == null) {
                layers[i].nodes[index] = new Layer(chars.length());
            }
            layers[i] = layers[i].nodes[index];
        }
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

    /**
     * Resize the layers array adding two to the current size.
     */
    private void resizeLayers() {
        Layer[] array = new Layer[layers.length + INCREMENT];
        System.arraycopy(layers, 0, array, 0, layers.length);
        layers = array;
    }

}
