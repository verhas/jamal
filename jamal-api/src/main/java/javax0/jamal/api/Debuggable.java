package javax0.jamal.api;

import java.util.List;
import java.util.Map;

public interface Debuggable {
    interface Scope {
        Map<String, Identified> getUdMacros();

        Map<String, Macro> getMacros();

        Delimiters getDelimiterPair();
    }

    interface MacroRegister {
        List<Scope> getScopes();

        List<Marker> getPoppedMarkers();
    }

    interface UserDefinedMacro {
        String[] getParameters();
        String getContent();
        String getOpenStr();
        String getCloseStr();
    }
}
