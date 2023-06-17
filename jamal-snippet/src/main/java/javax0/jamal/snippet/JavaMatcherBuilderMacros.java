package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.ObjectHolder;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;
import javax0.javalex.JavaLexed;
import javax0.javalex.LexMatcher;
import javax0.javalex.matchers.Lexpression;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import static javax0.jamal.tools.Params.holder;
import static javax0.javalex.LexpressionBuilder.anyTill;
import static javax0.javalex.LexpressionBuilder.character;
import static javax0.javalex.LexpressionBuilder.comment;
import static javax0.javalex.LexpressionBuilder.floatNumber;
import static javax0.javalex.LexpressionBuilder.group;
import static javax0.javalex.LexpressionBuilder.identifier;
import static javax0.javalex.LexpressionBuilder.integerNumber;
import static javax0.javalex.LexpressionBuilder.keyword;
import static javax0.javalex.LexpressionBuilder.list;
import static javax0.javalex.LexpressionBuilder.match;
import static javax0.javalex.LexpressionBuilder.not;
import static javax0.javalex.LexpressionBuilder.number;
import static javax0.javalex.LexpressionBuilder.oneOf;
import static javax0.javalex.LexpressionBuilder.oneOrMore;
import static javax0.javalex.LexpressionBuilder.optional;
import static javax0.javalex.LexpressionBuilder.string;
import static javax0.javalex.LexpressionBuilder.unordered;
import static javax0.javalex.LexpressionBuilder.zeroOrMore;

public class JavaMatcherBuilderMacros {

    private static class MatcherObjectHolder implements Identified, ObjectHolder<BiFunction<JavaLexed, Lexpression, LexMatcher>> {

        private static final AtomicInteger counter = new AtomicInteger(0);

        private final BiFunction<JavaLexed, Lexpression, LexMatcher> matcher;
        private final String id;

        private MatcherObjectHolder(final BiFunction<JavaLexed, Lexpression, LexMatcher> matcher) {
            this.matcher = matcher;
            this.id = "matcher" + counter.getAndIncrement();
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public BiFunction<JavaLexed, Lexpression, LexMatcher> getObject() {
            return matcher;
        }
    }

    private static String paddedId(final Identified macro) {
        return " " + macro.getId() + " ";
    }

    private static class Options {
        final Params.Param<Boolean> string;
        final Params.Param<String> groupName;
        final Params.Param<Pattern> pattern;


        private Options(final Macro macro, final Input in, final Processor processor) throws BadSyntax {
            string = holder(null, "string").asBoolean();
            groupName = holder(null, "name").asString().orElseNull();
            pattern = holder(null, "pattern").asPattern().orElseNull();
            Scan.using(processor).from(macro).between("()").keys(string, groupName, pattern).parse(in);
        }
    }

    private static BiFunction<JavaLexed, Lexpression, LexMatcher>[] matchers(final Options o, final Input in, final Processor processor) throws BadSyntax {
        if (o.string.is()) {
            return new BiFunction[]{match(in.toString())};
        }
        final var m = new ArrayList<BiFunction<JavaLexed, Lexpression, LexMatcher>>();
        while (!in.isEmpty()) {
            InputHandler.skipWhiteSpaces(in);
            if (!in.isEmpty()) {
                final var id = InputHandler.fetchId(in);
                m.add(getMatcherFromMacro(processor, id));
            }
        }
        return m.toArray(BiFunction[]::new);
    }

    public static BiFunction getMatcherFromMacro(final Processor processor, final String id) throws BadSyntax {
        return processor.getRegister()
                .getUserDefined(id)
                .filter(p -> p instanceof ObjectHolder)
                .map(p -> (ObjectHolder<BiFunction>) p)
                .map(ObjectHolder::getObject)
                .orElseThrow(() -> new BadSyntax("Macro " + id + " is not defined"));
    }


    private static BiFunction<JavaLexed, Lexpression, LexMatcher> matcher(final Options o, final Input in, final Processor processor) throws BadSyntax {
        if (o.string.is()) {
            return match(in.toString());
        }
        InputHandler.skipWhiteSpaces(in);
        final var id = InputHandler.fetchId(in);
        return getMatcherFromMacro(processor, id)
                ;
    }

    public static class Keyword implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final var id = in.toString().trim();
            final Identified macro;
            if (options.groupName.isPresent()) {
                macro = new MatcherObjectHolder(keyword(group(options.groupName.get()), id));
            } else {
                macro = new MatcherObjectHolder(keyword(id));
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());
            return paddedId(macro);
        }

        @Override
        public String getId() {
            return "j:keyword";
        }
    }

    public static class Not implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final Identified macro;
            if (options.groupName.isPresent()) {
                macro = new MatcherObjectHolder(not(group(options.groupName.get()), matchers(options, in, processor)));
            } else {
                macro = new MatcherObjectHolder(not(matchers(options, in, processor)));
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());
            return paddedId(macro);
        }

        @Override
        public String getId() {
            return "j:not";
        }
    }

    public static class AnyTill implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final Identified macro;
            if (options.groupName.isPresent()) {
                macro = new MatcherObjectHolder(anyTill(group(options.groupName.get()), matchers(options, in, processor)));
            } else {
                macro = new MatcherObjectHolder(anyTill(matchers(options, in, processor)));
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());
            return paddedId(macro);
        }

        @Override
        public String getId() {
            return "j:anyTill";
        }
    }


    //<editor-fold id="stringType">
    /*
     * This is generated code. DO NOT edit manually.
     */

    public static class StringMacro implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final var s = in.toString().trim();
            final Identified macro;
            if (options.groupName.isPresent()) {
                if (options.pattern.isPresent()) {
                    macro = new MatcherObjectHolder(string(group(options.groupName.get()), options.pattern.get()));
                } else {
                    if (s.length() > 0) {
                        macro = new MatcherObjectHolder(string(group(options.groupName.get()), s));
                    } else {
                        macro = new MatcherObjectHolder(string(group(options.groupName.get())));
                    }
                }
            } else {
                if (s.length() > 0) {
                    macro = new MatcherObjectHolder(string(s));
                } else {
                    macro = new MatcherObjectHolder(string());
                }
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());

            return paddedId(macro);
        }

        private static final String[] IDS = new String[]{"j:string"};

        @Override
        public String[] getIds() {
            return IDS;
        }
    }


    public static class Identifier implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final var s = in.toString().trim();
            final Identified macro;
            if (options.groupName.isPresent()) {
                if (options.pattern.isPresent()) {
                    macro = new MatcherObjectHolder(identifier(group(options.groupName.get()), options.pattern.get()));
                } else {
                    if (s.length() > 0) {
                        macro = new MatcherObjectHolder(identifier(group(options.groupName.get()), s));
                    } else {
                        macro = new MatcherObjectHolder(identifier(group(options.groupName.get())));
                    }
                }
            } else {
                if (s.length() > 0) {
                    macro = new MatcherObjectHolder(identifier(s));
                } else {
                    macro = new MatcherObjectHolder(identifier());
                }
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());

            return paddedId(macro);
        }

        private static final String[] IDS = new String[]{"j:identifier"};

        @Override
        public String[] getIds() {
            return IDS;
        }
    }


    public static class CharacterMacro implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final var s = in.toString().trim();
            final Identified macro;
            if (options.groupName.isPresent()) {
                if (options.pattern.isPresent()) {
                    macro = new MatcherObjectHolder(character(group(options.groupName.get()), options.pattern.get()));
                } else {
                    if (s.length() > 0) {
                        macro = new MatcherObjectHolder(character(group(options.groupName.get()), s));
                    } else {
                        macro = new MatcherObjectHolder(character(group(options.groupName.get())));
                    }
                }
            } else {
                if (s.length() > 0) {
                    macro = new MatcherObjectHolder(character(s));
                } else {
                    macro = new MatcherObjectHolder(character());
                }
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());

            return paddedId(macro);
        }

        private static final String[] IDS = new String[]{"j:character","j:char"};

        @Override
        public String[] getIds() {
            return IDS;
        }
    }


    public static class Comment implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final var s = in.toString().trim();
            final Identified macro;
            if (options.groupName.isPresent()) {
                if (options.pattern.isPresent()) {
                    macro = new MatcherObjectHolder(comment(group(options.groupName.get()), options.pattern.get()));
                } else {
                    if (s.length() > 0) {
                        macro = new MatcherObjectHolder(comment(group(options.groupName.get()), s));
                    } else {
                        macro = new MatcherObjectHolder(comment(group(options.groupName.get())));
                    }
                }
            } else {
                if (s.length() > 0) {
                    macro = new MatcherObjectHolder(comment(s));
                } else {
                    macro = new MatcherObjectHolder(comment());
                }
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());

            return paddedId(macro);
        }

        private static final String[] IDS = new String[]{"j:comment"};

        @Override
        public String[] getIds() {
            return IDS;
        }
    }



    //</editor-fold>

    public static class Match implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final var s = in.toString().trim();
            final Identified macro;
            if (options.groupName.isPresent()) {
                macro = new MatcherObjectHolder(match(group(options.groupName.get()), s));
            } else {
                macro = new MatcherObjectHolder(match(s));
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());

            return paddedId(macro);
        }

        @Override
        public String getId() {
            return "j:match";
        }
    }


    public static class OptionalMacro implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final var macro = new MatcherObjectHolder(optional(matcher(options, in, processor)));
            processor.define(macro);
            processor.getRegister().export(macro.getId());
            return paddedId(macro);
        }

        @Override
        public String getId() {
            return "j:optional";
        }
    }

    public static class OneOf implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final Identified macro;
            if (options.groupName.isPresent()) {
                macro = new MatcherObjectHolder(oneOf(group(options.groupName.get()), matchers(options, in, processor)));
            } else {
                macro = new MatcherObjectHolder(oneOf(matchers(options, in, processor)));
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());
            return paddedId(macro);
        }

        @Override
        public String getId() {
            return "j:oneOf";
        }
    }

    public static class Unordered implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final Identified macro;
            if (options.groupName.isPresent()) {
                macro = new MatcherObjectHolder(unordered(group(options.groupName.get()), matchers(options, in, processor)));
            } else {
                macro = new MatcherObjectHolder(unordered(matchers(options, in, processor)));
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());
            return paddedId(macro);
        }

        @Override
        public String getId() {
            return "j:unordered";
        }
    }

    public static class List implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final Identified macro;
            if (options.groupName.isPresent()) {
                macro = new MatcherObjectHolder(list(group(options.groupName.get()), matchers(options, in, processor)));
            } else {
                macro = new MatcherObjectHolder(list(matchers(options, in, processor)));
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());
            return paddedId(macro);
        }

        @Override
        public String getId() {
            return "j:list";
        }
    }

    public static class ZeroOrMore implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final Identified macro;
            if (options.groupName.isPresent()) {
                macro = new MatcherObjectHolder(zeroOrMore(group(options.groupName.get()), matcher(options, in, processor)));
            } else {
                macro = new MatcherObjectHolder(zeroOrMore(matcher(options, in, processor)));
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());
            return paddedId(macro);
        }

        @Override
        public String getId() {
            return "j:zeroOrMore";
        }
    }

    public static class OneOrMore implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            final Identified macro;
            if (options.groupName.isPresent()) {
                macro = new MatcherObjectHolder(oneOrMore(group(options.groupName.get()), matcher(options, in, processor)));
            } else {
                macro = new MatcherObjectHolder(oneOrMore(matcher(options, in, processor)));
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());
            return paddedId(macro);
        }

        @Override
        public String getId() {
            return "j:oneOrMore";
        }
    }

    public static class FloatMacro implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            BadSyntax.when(in.toString().trim().length() > 0, "j:float does not take any arguments");
            final Identified macro;
            if (options.groupName.isPresent()) {
                macro = new MatcherObjectHolder(floatNumber(group(options.groupName.get())));

            } else {
                macro = new MatcherObjectHolder(floatNumber());
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());
            return paddedId(macro);
        }

        @Override
        public String getId() {
            return "j:float";
        }
    }

    public static class IntegerMacro implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            BadSyntax.when(in.toString().trim().length() > 0, "j:int does not take any arguments");
            final Identified macro;
            if (options.groupName.isPresent()) {
                macro = new MatcherObjectHolder(integerNumber(group(options.groupName.get())));

            } else {
                macro = new MatcherObjectHolder(integerNumber());
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());
            return paddedId(macro);
        }
    }

    public static class NumberMacro implements Macro {
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var options = new Options(this, in, processor);
            BadSyntax.when(in.toString().trim().length() > 0, "j:number does not take any arguments");
            final Identified macro;
            if (options.groupName.isPresent()) {
                macro = new MatcherObjectHolder(number(group(options.groupName.get())));

            } else {
                macro = new MatcherObjectHolder(number());
            }
            processor.define(macro);
            processor.getRegister().export(macro.getId());
            return paddedId(macro);
        }


        @Override
        public String getId() {
            return "j:number";
        }
    }

}