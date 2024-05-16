package javax0.jamal.tools.param;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Params;

import java.io.File;

public class FileParameter extends AbstractTypedParameter<String> {

    private File file = null;
    private boolean isRemote;
    private boolean isFile;

    public FileParameter(Params.Param<String> param) {
        super(param);
    }

    private void init() throws BadSyntax {
        if (file == null) {
            file = new File(get());
            isRemote = FileTools.isRemote(get());
            isFile = file.isFile();
        }
    }

    public FileParameter required() {
        checkDone(DoneAction.REQUIRED);
        return this;
    }

    public File file() throws BadSyntax {
        init();
        return file;
    }

    public boolean isRemote() throws BadSyntax {
        init();
        return isRemote;
    }

    public boolean isFile() throws BadSyntax {
        init();
        return isFile;
    }

    public String get() throws BadSyntax {
        return param.get();
    }

    public FileParameter defaultValue(String dV) {
        if (dV == null) {
            param.orElseNull();
        } else {
            param.defaultValue(dV);
        }
        return this;
    }
}
