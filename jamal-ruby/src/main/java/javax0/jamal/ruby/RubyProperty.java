package javax0.jamal.ruby;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import org.jruby.Ruby;
import org.jruby.RubyComplex;
import org.jruby.RubyFixnum;
import org.jruby.RubyFloat;
import org.jruby.RubyRational;
import org.jruby.RubyString;
import org.jruby.RubySymbol;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public class RubyProperty implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var shell = Shell.getShell(processor);
        InputHandler.skipWhiteSpaces(in);
        final var id = InputHandler.fetchId(in);
        InputHandler.skipWhiteSpaces(in);
        if (in.length() == 0) {
            return "" + shell.property(id);
        }
        if (InputHandler.firstCharIs(in, '=')) {
            InputHandler.skip(in, 1);
        } else {
            throw new BadSyntax("There must be a '=' after the name of the Ruby property to assign a value to it.");
        }
        InputHandler.skipWhiteSpaces(in);
        shell.property(id, cast(in.toString(), shell.shell.getProvider().getRuntime()));
        return "";
    }

    private static Object cast(String s, Ruby ruby) throws BadSyntax {
        try {
            return
                cast(s, "to_sym", k -> RubySymbol.newSymbol(ruby, k)).orElseGet(() ->
                    cast(s, "to_r", k -> toRational(ruby, k)).orElseGet(() ->
                        cast(s, "to_f", k -> RubyFloat.newFloat(ruby, Double.parseDouble(k))).orElseGet(() ->
                            cast(s, "to_i", k -> RubyFixnum.newFixnum(ruby, Long.parseLong(k))).orElseGet(() ->
                                cast(s, "to_c", k -> toComplex(ruby, k)).orElseGet(() ->
                                    cast(s, "to_c/i", k -> toComplexInt(ruby, k)).orElseGet(() ->
                                        cast(s, "to_s", k -> RubyString.newString(ruby, k)).orElseGet(
                                            () -> RubyString.newString(ruby, s)
                                        )
                                    )
                                )
                            )
                        )
                    )
                );
        } catch (IllegalArgumentException e) {
            throw new BadSyntax(e.getMessage());
        } catch (Exception e) {
            throw new BadSyntax("There was an error during casting the value '" + s + "'", e);
        }
    }

    private static final RubyRational toRational(Ruby ruby, final String s) {
        final var parts = s.split("/", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Ruby rational has to be 'number / number' format and '" + s + "' is not.");
        }
        return RubyRational.newRational(ruby, Long.parseLong(parts[0].trim()), Long.parseLong(parts[1].trim()));
    }

    private static final String FLOAT_REGEX = "[-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?";
    private static final String INT_REGEX = "[-+]?[0-9]*";
    private static final Pattern FLOAT_PATTERN = Pattern.compile("(" + FLOAT_REGEX + ")\\s*\\+\\s*(" + FLOAT_REGEX + ")\\s*i");
    private static final Pattern INT_PATTERN = Pattern.compile("(" + INT_REGEX + ")\\s*\\+\\s*(" + INT_REGEX + ")\\s*i");

    private static final RubyComplex toComplex(Ruby ruby, final String s) {
        final var matcher = FLOAT_PATTERN.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Ruby complex has to be 'R+Ci' format and '" + s + "' is not.");
        }
        return RubyComplex.newComplexRaw(ruby, RubyFloat.newFloat(ruby, Double.parseDouble(matcher.group(1))),
            RubyFloat.newFloat(ruby, Double.parseDouble(matcher.group(2))));
    }

    private static final RubyComplex toComplexInt(Ruby ruby, final String s) {
        final var matcher = INT_PATTERN.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Ruby complex has to be 'R+Ci' format and '" + s + "' is not.");
        }
        return RubyComplex.newComplexRaw(ruby, RubyFixnum.newFixnum(ruby, Long.parseLong(matcher.group(1))),
            RubyFixnum.newFixnum(ruby, Long.parseLong(matcher.group(2))));
    }

    private static Optional<Object> cast(String s, String prefix, Function<String, Object> converter) {
        prefix = "(" + prefix + ")";
        if (s.startsWith(prefix)) {
            return Optional.of(converter.apply(s.substring(prefix.length()).replaceAll("^\\s*", "")));
        }
        return Optional.empty();
    }

    @Override
    public String getId() {
        return "ruby:property";
    }
}
