package javax0.jamal.doclet;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

/**
 * Poor men's implementation of the link tag.
 * <p>
 * It creates the link based on the text and does not look into the code if that really exists or not. That way
 * most of the links that point to some method, which has arguments will lead to an invalid link because there is
 * no easy way to get the full name of the parameter classes. These are usually added by the IDE as simple names
 * when the classes are imported into the application.
 */
@Macro.Stateful
public class Link implements Macro {

    String currentClass;

    @Override
    public String evaluate(Input in, Processor processor) {
        InputHandler.skipWhiteSpaces(in);
        final var str = in.toString();
        final var clIndex = str.indexOf(')');
        final int spcIndex;
        if (clIndex > -1) {
            spcIndex = str.indexOf(' ', clIndex);
        } else {
            spcIndex = str.indexOf(' ');
        }

        final String ref;
        if (spcIndex > -1) {
            ref = str.substring(0, spcIndex);
        } else {
            ref = str;
        }
        final String klass;
        final String member;
        if (ref.contains("#")) {
            final var refParts = ref.split("#", 2);
            klass = refParts[0];
            if (refParts.length > 1) {
                member = refParts[1];
            } else {
                member = null;
            }
        } else {
            klass = ref;
            member = null;
        }
        final String text;
        if (spcIndex > 1) {
            text = str.substring(spcIndex);
        } else {
            if (member != null) {
                text = member;
            } else {
                final var dot = klass.lastIndexOf('.');
                if (dot > -1) {
                    text = klass.substring(dot + 1);
                } else {
                    text = klass;
                }
            }
        }
        final String memberName;
        final String memberArgs;
        final var opIndex = member == null ? -1 : member.indexOf('(');
        if (opIndex > -1) {
            memberName = member.substring(0, opIndex);
            memberArgs = member.substring(opIndex);
        } else {
            memberName = member;
            memberArgs = "";
        }
        final String url;
        if (currentClass != null && currentClass.endsWith(klass)) {
            if (member != null && member.length() > 0) {
                if (klass.endsWith(memberName)) {
                    url = "#%3Cinit" + memberArgs + "%3E";
                } else {
                    url = "#" + member;
                }
            } else {
                url = ".";
            }
        } else {
            url = klass.replaceAll("[\\w\\d]+\\.", "../").replaceAll("\\w+$", "") + klass.replaceAll("\\.", "/")
                    + ".html"
                    + (member == null ? "" : "#" + member.replaceAll(" ", ""));
        }
        return "<a href=\"" + url + "\"><code>" + text + "</code></a>";
    }
}
