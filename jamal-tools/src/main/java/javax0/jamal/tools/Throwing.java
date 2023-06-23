package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;

import java.util.function.Predicate;

public class Throwing<T> {

    public interface Supplier<Z> {
        Z get() throws Exception;
    }

    public interface Function<Z, R> {
        R apply(Z z) throws Exception;
    }

    public interface Consumer<Z> {
        void accept(Z z) throws Exception;
    }


    T object;
    java.util.function.Supplier<String> message;

    private Throwing(T object) {
        this.object = object;
    }


    public static <T> Throwing<T> of(Supplier<T> object, java.util.function.Supplier<String> message) throws BadSyntax {
        try {
            final var it = new Throwing<>(object.get());
            return it;
        } catch (Exception e) {
            throw new BadSyntax(message.get(), e);
        }
    }

    public static <T> Throwing<T> of(Supplier<T> object, String hurlMessage, Object... parameters) throws BadSyntax {
        return of(object, () -> String.format(hurlMessage, parameters));
    }

    public static <T> Throwing<T> of(Supplier<T> object) throws BadSyntax {
        return of(object, () -> "");
    }

    public Throwing<T> hurl(java.util.function.Supplier<String> message) {
        final var it = new Throwing<>(object);
        it.message = message;
        return it;
    }

    public Throwing<T> hurl(String format, Object... parameters) {
        return hurl(() -> String.format(format, parameters));
    }

    public Throwing<T> when(boolean condition) throws BadSyntax {
        if (condition) {
            throw new BadSyntax(message.get());
        }
        return this;
    }

    public Throwing<T> when(Predicate<T> condition) throws BadSyntax {
        return when(condition.test(object));
    }

    public Throwing<T> when(boolean condition, Consumer<T> consumer) throws BadSyntax {
        if (condition) {
            try {
                consumer.accept(object);
            } catch (Exception e) {
                throw new BadSyntax(message.get(), e);
            }
        }
        return this;
    }

    public Throwing<T> when(Predicate<T> condition, Consumer<T> consumer) throws BadSyntax {
        return when(condition.test(object), consumer);
    }

    public <K> Throwing<T> when(Class<K> condition, Consumer<K> consumer) throws BadSyntax {
        if (condition.isAssignableFrom(object.getClass())) {
            try {
                consumer.accept(condition.cast(object));
            } catch (Exception e) {
                throw new BadSyntax(message.get(), e);
            }
        }
        return this;
    }

    public <K> Throwing<K> map(Function<T, K> function) throws BadSyntax {
        return of(() -> function.apply(object), message);
    }

    public <K> Throwing<K> map(Function<T, K> function, java.util.function.Supplier<String> message) throws BadSyntax {
        return of(() -> function.apply(object), message);
    }

    public <K> Throwing<K> map(Function<T, K> function, String hurlMessage, Object... parameters) throws BadSyntax {
        return of(() -> function.apply(object), () -> String.format(hurlMessage, parameters));
    }

    public <K> Throwing<K> cast(Class<K> klass) throws BadSyntax {
        return of(() -> klass.cast(object), message);
    }

    public T get() {
        return object;
    }
}
