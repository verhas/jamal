package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

class ThrowingTest {

    @Test
    void testHurl() throws BadSyntax {
        String message = "Test Exception";
        final var throwing = Throwing.of(() -> "Hello").hurl(message);
        Assertions.assertThrows(BadSyntax.class, () -> throwing.when(true));
        Assertions.assertEquals(message, Assertions.assertThrows(BadSyntax.class, () -> throwing.when(true)).getMessage());
    }

    @Test
    void testHurlWithFormat() throws BadSyntax {
        String format = "Test Exception: %s";
        String parameter = "Custom Message";
        final var throwing = Throwing.of(() -> "Hello").hurl(format, parameter);
        Assertions.assertThrows(BadSyntax.class, () -> throwing.when(true));
        Assertions.assertEquals(String.format(format, parameter), Assertions.assertThrows(BadSyntax.class, () -> throwing.when(true)).getMessage());
    }

    @Test
    void testWhenConditionTrue() throws BadSyntax {
        Assertions.assertThrows(BadSyntax.class, () -> Throwing.of(() -> "Hello").hurl("").when(true));
    }

    @Test
    void testWhenConditionFalse() throws BadSyntax {
        final var throwing = Throwing.of(() -> "Hello").hurl("").when(false);
        Assertions.assertEquals("Hello", throwing.get());
    }

    @Test
    void testWhenPredicateTrue() throws BadSyntax {
        Predicate<String> predicate = s -> s.length() > 5;
        Assertions.assertThrows(BadSyntax.class, () -> Throwing.of(() -> "Helloo").hurl("").when(predicate));
    }

    @Test
    void testWhenPredicateFalse() throws BadSyntax {
        Predicate<String> predicate = s -> s.length() > 5;
        final var throwing = Throwing.of(() -> "Hi").hurl("").when(predicate);
        Assertions.assertEquals("Hi", throwing.get());
    }

    @Test
    void testWhenConditionTrueWithConsumer() throws BadSyntax {
        Assertions.assertThrows(BadSyntax.class, () -> Throwing.of(() -> "Hello").when(true, s -> {
            throw new RuntimeException("Test Exception");
        }));
    }

    @Test
    void testWhenConditionFalseWithConsumer() throws BadSyntax {
        final var throwing = Throwing.of(() -> "Hello").when(false, s -> {
            throw new RuntimeException("Test Exception");
        });
        Assertions.assertEquals("Hello", throwing.get());
    }

    @Test
    void testWhenPredicateTrueWithConsumer() throws BadSyntax {
        Predicate<String> predicate = s -> s.length() > 5;
        Throwing.of(() -> "Hello").when(predicate, s -> {
            throw new RuntimeException("Test Exception");
        });
    }

    @Test
    void testWhenPredicateFalseWithConsumer() throws BadSyntax {
        Predicate<String> predicate = s -> s.length() > 5;
        final var throwing = Throwing.of(() -> "Hi").when(predicate, s -> {
        });
        Assertions.assertEquals("Hi", throwing.get());
        // no exception occurred
    }

    @Test
    void testWhenClassConditionTrue() throws BadSyntax {
        final var closure = new Object() {
            boolean executed = false;
        };
        final var throwing = Throwing.of(() -> 13).hurl("Cannot cast to String").cast(String.class).when(String.class, s -> {
            closure.executed = true;
        });
        Assertions.assertTrue(closure.executed);
    }

    @Test
    void testWhenClassConditionFalse() throws BadSyntax {
        final var throwing = Throwing.of(() -> "Hello").when(Integer.class, s -> {
            throw new RuntimeException("Test Exception");
        });
        Assertions.assertEquals("Hello", throwing.get());
    }

    @Test
    void testMap() throws BadSyntax {
        final var throwing = Throwing.of(() -> "Hello").map(s -> s + " World");
        Assertions.assertEquals("Hello World", throwing.get());
    }

    @Test
    void testMapWithException() throws BadSyntax {
        Assertions.assertThrows(BadSyntax.class, () -> Throwing.of(() -> "Hello").map(s -> {
            throw new RuntimeException("Test Exception");
        }));
    }

    @Test
    void testMapWithConsumer() throws BadSyntax {
        final var throwing = Throwing.of(() -> "Hello").map(s -> s, () -> "Test Exception");
        Assertions.assertEquals("Hello", throwing.get());
    }

    @Test
    void testCast() throws BadSyntax {
        final var throwing = Throwing.of(() -> "Hello")
                .hurl("Test Exception")
                .cast(Object.class);
        Assertions.assertEquals("Hello", throwing.get());
    }

    @Test
    void testCastWithException() throws BadSyntax {
        Assertions.assertThrows(BadSyntax.class, () -> Throwing.of(() -> "Hello")
                .hurl("Test Exception")
                .cast(Integer.class));
    }
}

