package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.poifs.crypt.HashAlgorithm;

import java.util.Arrays;

public class MacroDocxReadOnly implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var password = Params.<Boolean>holder(null, "pass","password").asString();
        final var algo = Params.<Boolean>holder(null, "alg", "algo", "algorithm").asString();
        Scan.using(processor).from(this).tillEnd().keys(password, algo).parse(in);
        if( password.isPresent() != algo.isPresent() ) {
            throw new BadSyntax("The parameter 'algorithm' and 'password' must be used together.");
        }
        final var context = processor.getContext();
        if( context instanceof XWPFContext){
            final var xwpfContext = (XWPFContext) context;
            if(xwpfContext.readOnly){
                if( xwpfContext.password == null ) {
                    throw new BadSyntax("The document is read only already.");
                }else{
                    throw new BadSyntax(String.format("The document is read only already, and the password is '%s'", xwpfContext.password));
                }
            }
            xwpfContext.readOnly = true;
            if( password.isPresent() ) {
                xwpfContext.password = password.get();
                try {
                    xwpfContext.hashAlgorithm = HashAlgorithm.fromEcmaId(algo.get());
                }catch(EncryptedDocumentException e) {
                    throw new BadSyntax(String.format("The '%s' is not a valid ECMA ID for any of the implemented algorithms.\nThe valid argorithms are: %s",
                            algo.get(),
                            String.join(",", Arrays.stream(HashAlgorithm.values()).map(a -> a.ecmaString).toArray(String[]::new)).substring(1)));
                }
            }
        }else{
            throw new BadSyntax("The macro 'docx:readOnly' can only be used in DOCX files.");
        }
        return "";
    }

    @Override
    public String getId() {
        return "docx:readOnly";
    }
}
