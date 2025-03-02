package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.util.Arrays;
import java.util.function.Consumer;

/*
 *
 * tag::protect[]
 * {%macro docx::protect%}
 *
 * Using this macro you can set the protection of a document.
 * The syntax of the macro is
 *
 * [source]
 * ----
 *   {@docx:protect [password=XXX algorithm=YYY] (trackChanges|readOnly|forms|comments) }
 * ----
 *
 * You can set a password and an encoding algorithm using the options `password` and `algorithm`.
 * If these options are not present, then the user can switch the protection off.
 * When you specify the password and the algorithm then the user can switch off the protection only if they know the password.
 *
 * The options `trackChanges`, `readOnly`, `forms`, and `comments` are mutually exclusive.
 * Exactly one of them has to be specified.
 *
 * end::protect[]
 */
@Macro.Name("docx:protect")
public
class MacroDocxProtect implements Macro, Scanner.WholeInput {
    private static class CallBack implements XWPFContext.DocxTerminalCallBack {
        private XWPFDocument document;
        String password;
        HashAlgorithm hashAlgorithm = HashAlgorithm.none;
        Type t;

        @Override
        public void setDocument(final XWPFDocument document) {
            this.document = document;
        }

        @Override
        public void process() {
            if (password != null) {
                t.enforcer.accept(document, password, hashAlgorithm);
            } else {
                t.simpleEnforcer.accept(document);
            }
        }
    }

    private interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    private enum Type {
        TRACK(1, XWPFDocument::enforceTrackedChangesProtection, XWPFDocument::enforceTrackedChangesProtection),
        READONLY(2, XWPFDocument::enforceReadonlyProtection, XWPFDocument::enforceReadonlyProtection),
        COMMENT(3, XWPFDocument::enforceCommentsProtection, XWPFDocument::enforceCommentsProtection),
        FORMS(4, XWPFDocument::enforceFillingFormsProtection, XWPFDocument::enforceFillingFormsProtection);

        static Type of(int ordinal) throws BadSyntax {
            return Arrays.stream(Type.values())
                    .filter(t -> t.ordinal == ordinal)
                    .findFirst()
                    .orElseThrow(() -> new BadSyntax(String.format("Unknown protection type: %d", ordinal)));
        }

        private final int ordinal;
        private final TriConsumer<XWPFDocument, String, HashAlgorithm> enforcer;
        private final Consumer<XWPFDocument> simpleEnforcer;

        Type(int ordinal, TriConsumer<XWPFDocument, String, HashAlgorithm> enforcer, Consumer<XWPFDocument> simpleEnforcer) {
            this.ordinal = ordinal;
            this.enforcer = enforcer;
            this.simpleEnforcer = simpleEnforcer;
        }
    }

    private static int x(boolean b) {
        return b ? 1 : 0;
    }

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // tag::protect_options[]
        final var password = scanner.str(null, "pass", "password");
        // This option specifies the password needed to switch off the protection.
        // If the option is not specified, then the user can switch off the protection with just a few mouse clicks.
        final var algo = scanner.str(null, "alg", "algo", "algorithm");
        // This option specifies the algorithm used to encode the password.
        // The name of the algorithm is the official ECMA name of the algorithm.
        // Any of the algorithms can be used implemented by the DOCX format and supported by the underlying Apache POI library.
        // The algorithm names are:
        //
        // * `SHA1`
        // * `SHA256`
        // * `SHA384`
        // * `SHA512`
        // * `MD5`
        // * `MD2`
        // * `MD4`
        // * `RIPEMD-128`
        // * `RIPEMD-160`
        // * `WHIRLPOOL`
        // * `SHA224`
        // * `RIPEMD-256`
        //
        // The list of algorithms may change in the future.
        // To see the actual list of algorithms, specify an invalid algorithm.
        // The error will list all the currently available algorithms.
        final var track = scanner.bool(null, "track", "trackChanges");
        // Specify the protection level so that the document can be changed without changing the state of change tracking.
        // Reasonably, you want to have the tracking switched on.
        // To do that you can switch it on in the source document or use the `docx:trackChanges` macro without the `off` option.
        final var readOnly = scanner.bool(null, "read", "readOnly", "readonly");
        // Specify the protection level so that the document is read only.
        final var comments = scanner.bool(null, "comments");
        // Specify the protection level so that the user can edit only the comments.
        final var forms = scanner.bool(null, "forms");
        // Specify the protection level so that the user can edit only the forms of the document.
        // end::protect_options[]
        scanner.done();
        BadSyntax.when(x(track.is()) + x(readOnly.is()) + x(comments.is()) + x(forms.is()) != 1, "Exactly one of the protection types must be specified.");
        final var t = Type.of(x(track.is()) + x(readOnly.is()) * 2 + x(comments.is()) * 3 + x(forms.is()) * 4);
        BadSyntax.when(password.isPresent() != algo.isPresent(), "The parameter 'algorithm' and 'password' must be used together.");
        final var context = XWPFContext.getXWPFContext(processor);
        final var callBack = new CallBack();
        callBack.t = t;
        if (password.isPresent()) {
            callBack.password = password.get();
            try {
                callBack.hashAlgorithm = HashAlgorithm.fromEcmaId(algo.get());
            } catch (EncryptedDocumentException e) {
                throw new BadSyntax(String.format("The '%s' is not a valid ECMA ID for any of the implemented algorithms.\nThe valid argorithms are: %s",
                        algo.get(),
                        String.join(",", Arrays.stream(HashAlgorithm.values()).map(a -> a.ecmaString).toArray(String[]::new)).substring(1)));
            }
        }
        context.register(callBack);
        return "";
    }

}
