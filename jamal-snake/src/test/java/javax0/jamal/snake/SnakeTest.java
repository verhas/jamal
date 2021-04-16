package javax0.jamal.snake;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.Map;

public class SnakeTest {
    public static class Q {
        public final String s;

        public Q(String s) {
            this.s = s;
        }
    }

    @Test
    void teswt() {
        final var yaml = new Yaml();
        final var str = "" +
            "a: 1\n" +
            "b: |\n" +
            "  z2\n" +
            "  zsuzsa baba\n" +
            "# this is a comment\n" +
            "s:\n" +
            "  k:\n" +
            "    - kukk\n" +
            "    - kakk\n" +
            "    - makk\n" +
            "    - wupsy: wupsy\n" +
            "      dosy: dosy\n" +
            "        - 2001-07-23\n" +
            "      ? [ New York Yankees,Atlanta Braves ]\n" +
            "      : [ 2001-07-02, 2001-08-12, 2001-08-14 ]\n" +
            "  t: !!javax0.jamal.api.Ref od002\n" +
            "  h: zumm";
        final var h = yaml.load(str);
        System.out.println(h);
        System.out.println(str);
        ((Map) ((Map) h).get("s")).put("me", h);
        ((Map) ((Map) h).get("s")).put("ma", "*id002");
        final var out = new StringWriter();
        yaml.dump(h, out);
        System.out.println(out);
    }
}
