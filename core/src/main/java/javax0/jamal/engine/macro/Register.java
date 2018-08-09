package javax0.jamal.engine.macro;

import javax0.jamal.engine.UserDefinedMacro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Register {
    private ArrayList<Map<String, UserDefinedMacro>> macroStack = new ArrayList<>();

    public Optional<UserDefinedMacro> get(String id) {
        for( int level = macroStack.size()-1 ; level > -1 ; level -- ) {
            var map = macroStack.get(level);
            if( map.containsKey(id)){
                return Optional.of(map.get(id));
            }
        }
        return Optional.empty();
    }

    public void put(UserDefinedMacro macro) {
        macroStack.get(macroStack.size()-1).put(macro.getId(), macro);
    }

    public void push() {
        macroStack.add(new HashMap<>());
    }

    public void pop() {
        macroStack.remove(macroStack.size()-1);
    }


}
