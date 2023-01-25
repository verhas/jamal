package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

/*
snippet unicode

This macro can insert unicode characters into the output.
The unicode character can be specified by its code point.
The code point can be specified in decimal or hexadecimal.
The code point can be specified with the prefix `&#x` or `&#`.
The prefix `&#x` is the default.

Examples:

{%sample/
0x{@unicode &#x43}\
{@unicode &#x41}\
{@unicode &#x46}\
{@unicode &#x45}\
{@unicode &#x42}\
{@unicode &#x41}\
{@unicode &#x42}\
{@unicode &#x45}
%}

will result

{%output%}

The macro is also supported by a resource file named `tab.jim`.
You can import this file as `{@import res:tab.jim}`.
The constants defined in this file are:

{%@import src/main/resources/tab.jim%}

{%!#replaceLines replace="|\\{#define\\s(\\w+).*|`$1`|" replace="|\\{@comment\\}||"
{%@include [verbatim] src/main/resources/tab.jim%}
%}

This are symbolic names for some non-printable characters

end snippet
 */

public class Unicode implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        var codeText = in.toString().trim();
        var radix = 16;
        if (codeText.startsWith("&#x")) {
            codeText = codeText.substring(3);
        } else if (codeText.startsWith("&#")) {
            codeText = codeText.substring(2);
            radix = 10;
        }
        try {
            final var codePoint = Integer.parseInt(codeText, radix);
            final var ch = Character.toChars(codePoint);
            if (ch.length == 1) {
                return "" + ch[0];
            }
            return "" + ch[0] + ch[1];
        } catch (Exception e) {
            throw new BadSyntax(String.format("The value %s is not a valid unicode character value.", codeText), e);
        }
    }
}
