import javax0.jamal.api.Macro;
import javax0.jamal.io.*;

module jamal.io {
    exports javax0.jamal.io;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    provides Macro with Write, Remove, Mkdir, Print, Cwd, OsName, Exec, Exec.WaitFor, IoFile;
}