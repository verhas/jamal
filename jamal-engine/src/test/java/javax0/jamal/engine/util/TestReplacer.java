package javax0.jamal.engine.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestReplacer {

    @Test
    void replacesInOrder() {
        final var sut = new Replacer(Map.of("[", "{", "[[", "{%", "]", "}", "]]", "%}"), "nothing at all");
        Assertions.assertEquals("{%@define ala={bele} %}", sut.replace("[[@define ala=[bele] ]]"));
    }

    @Test
    void replaceEscaped() {
        final var sut = new Replacer(Map.of(
            "[[", "[[@escape  `a`[[`a`]]",
            "]]", "[[@escape  `a`]]`a`]]",
            "[", "[[",
            "]", "]]"
        ), "[");
        Assertions.assertEquals("[[@escape`.`]`.`]] ]]", sut.replace("[@escape`.`]`.`] ]"));
        Assertions.assertEquals("[[@escape`.`]`.`[[@escape  `a`]]`a`]]", sut.replace("[@escape`.`]`.`]]"));
    }


}
