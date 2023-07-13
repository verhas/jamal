import javax0.jamal.api.Macro;
import javax0.jamal.io.Copy;
import javax0.jamal.io.Cwd;
import javax0.jamal.io.Exec;
import javax0.jamal.io.IoFile;
import javax0.jamal.io.Mkdir;
import javax0.jamal.io.OsName;
import javax0.jamal.io.Print;
import javax0.jamal.io.Remove;
import javax0.jamal.io.Write;

module jamal.io {
    exports javax0.jamal.io;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    provides Macro with Write, Remove, Mkdir, Print, Cwd, OsName, Exec, Exec.WaitFor, IoFile,Copy;
}