import javax0.jamal.api.Debugger;
import javax0.jamal.debugger.HttpServerDebugger;
import javax0.jamal.debugger.ServerTcpDebugger;

module jamal.debug {

    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;

    requires jdk.httpserver;

    provides Debugger with ServerTcpDebugger, HttpServerDebugger;
}