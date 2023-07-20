package javax0.jamal.decored;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class GetPostfixes {

    private static final int WORD_COUNT = 3000;
    private static final int MIN_PF_LENGTH = 3;
    private static final int MAX_PF_LENGTH = 5;

    @Test
    void getPostfixes() {
        String[] word = new String[WORD_COUNT];
        try (final var is = this.getClass().getClassLoader().getResourceAsStream("wordlist.txt");
             final var r = new InputStreamReader(is);
             final var br = new BufferedReader(r)) {
            int i = 0;
            String line;
            while ((line = br.readLine()) != null) {
                word[i++] = line;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final var postfixFrequency = new HashMap<String, Integer>();
        final var postfixes = new HashSet<String>();
        for (var postfixLength = MAX_PF_LENGTH; postfixLength >= MIN_PF_LENGTH; postfixLength--) {
            for (int i = 0; i < WORD_COUNT; i++) {
                final var w = word[i].substring(Math.max(0, word[i].length() - postfixLength));
                postfixFrequency.merge(w, 1, Integer::sum);
            }
            postfixes.addAll(postfixFrequency.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .filter(e -> e.getValue() > 10)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet()));
        }

        try (final var ps = new PrintStream(new FileOutputStream("./src/test/resources/postfixes.txt"))) {
            int i = 0;
            for (final var s :
                    postfixes.stream()
                            .map(GetPostfixes::reverse)
                            .sorted((e1,e2) -> e2.length() - e1.length())
                            .map(GetPostfixes::reverse).collect(Collectors.toList())) {
                ps.printf("\"%s\",", s);
                i++;
                if( i %8 == 0 ){
                    ps.println();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static String reverse(final String s) {
        final var sb = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.append(s.charAt(i));
        }
        return sb.toString();
    }
}


