package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ThrowingAPI {
    interface HasMessage<T> {

        NoMessage<T> when(boolean condition) throws BadSyntax;

        NoMessage<T> when(Predicate<T> condition) throws BadSyntax;

        NoMessage<T> when(boolean condition, Throwing.Consumer<T> consumer) throws BadSyntax;

        NoMessage<T> when(Predicate<T> condition, Throwing.Consumer<T> consumer) throws BadSyntax;

        <K> NoMessage<T> when(Class<K> condition, Throwing.Consumer<K> consumer) throws BadSyntax;

        <K> NoMessage<K> map(Throwing.Function<T, K> function) throws BadSyntax;


        <K> NoMessage<K> cast(Class<K> klass) throws BadSyntax;

        T get();

    }

    interface NoMessage<T> {
        NoMessage<T> when(boolean condition, Consumer<T> consumer) throws BadSyntax;

        NoMessage<T> when(Predicate<T> condition, Consumer<T> consumer) throws BadSyntax;

        <K> NoMessage<T> when(Class<K> condition, Consumer<K> consumer) throws BadSyntax;

        HasMessage<T> hurl(java.util.function.Supplier<String> message);

        HasMessage<T> hurl(String format, Object... parameters);
        <K> NoMessage<K> map(Function<T, K> function) throws BadSyntax;

        <K> NoMessage<K> map(Throwing.Function<T, K> function, java.util.function.Supplier<String> message) throws BadSyntax;

        <K> NoMessage<K> map(Throwing.Function<T, K> function, String hurlMessage, Object... parameters) throws BadSyntax;

        T get();

    }
}
