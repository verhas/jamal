package javax0.jamal.extensions;

import javax0.jamal.api.*;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Get an indexed element from a string table.
 *
 * <p> A string table is a string in which certain characters are
 * separators. These separators divide the string into substrings. The
 * substrings can also be string tables that use different separator
 * characters.
 *
 * <p> In the simplest one dimensional form a string table is a string
 * that is separated by {@code |} characters. Each element of the
 * table can itself be a table that uses the character {@code /}. The
 * tables in this string table are separated by {@code :}, then {@code
 * -} and finally {@code .} (dot). These are the default values for the
 * table separators on the different levels.
 *
 * <p> Usually there is no need for so many redirections. The macro
 * {@code get} has numeric arguments and then the string, which is a
 * string table. The numeric arguments are indices. The string should
 * start with the top level separator, which is {@code |} by default.
 *
 * <p> For example
 *
 * <pre>
 *     {{#get 1 2 |a/b/c|h/k/j|o/z}}
 * </pre>
 * <p>
 * will result {@code j} because that is the third element of the second
 * table. (Indexing starts with zero.)
 *
 * <p> The use of the macro can be handy when a for loop has to go
 * through linked elements. For example we want to iterate through some
 * methods and argument types attached to it. The following example is
 * from a real life project:
 *
 * <pre>
 *     {{#eval{{ @for nameOfTheMethod in (truncate|int|int,substring|int|int,
 *                                           between|String|String,mid|int|int,
 *                                           prependIfMissing|CharSequence|CharSequence...,
 *                                           pad|int|char)=
 *         public Chain {{#get 0 |nameOfTheMethod}}(final {{#get 1 |nameOfTheMethod}} arg1,final {{#get 2 |nameOfTheMethod}} arg2) {
 *             return copy(Str.this.{{#get 0 |nameOfTheMethod}}(arg1,arg2)).new Chain();
 *         } }}}}
 * </pre>
 *
 * <p> The separators by default are the characters {@code |/:-.} in
 * this order as the separation digs deeper along the indices. This
 * default can be overridden defining the user defined macro {@code
 * $getsep}. When this macro is defined the first character will be used
 * as a separator character on the top level, the second on the next and
 * so on.
 */
@Macro.Name("get")
public
class IndexStringTable implements Macro {
    private static final String DEFAULT = "|/:-.";

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {

        final var optionalForSepMacro = processor.getRegister().getUserDefined("$getsep");
        final var splitters = optionalForSepMacro
                .filter(ud -> ud instanceof Evaluable)
                .map(ud -> (Evaluable) ud)
                .map(udm -> {
                    try {
                        return udm.evaluate();
                    } catch (BadSyntax bs) {
                        return DEFAULT;
                    }
                })
                .orElse(DEFAULT);
        BadSyntax.when(splitters.length() == 0, "$getsep defines a zero length separator");
        final var input = in.toString();
        final var index = input.indexOf(splitters.charAt(0));
        BadSyntax.when(index == -1, "There is no separator '%s' in the input.", splitters.charAt(0));
        final var indices = input.substring(0, index);
        var table = input.substring(index + 1);
        final var indexArray = Arrays.stream(indices.trim().split("\\s+")).mapToInt(Integer::parseInt).map(Math::abs).toArray();
        for (int i = 0; i < indexArray.length; i++) {
            BadSyntax.when(i >= splitters.length(), "There are too many indices to get data from a string table");
            final var splitter = splitters.substring(i, i + 1);
            final var cols = table.split(Pattern.quote(splitter));
            final var table_ = table;
            final var i_ = i;
            BadSyntax.when(indexArray[i] >= cols.length, () -> String.format("There are only %d columns in the string \"%s\" using the splitter %s and macro 'get' wants to access %d",
                    cols.length, table_, splitter, indexArray[i_]));
            table = cols[indexArray[i]];
        }
        return table;
    }

}
