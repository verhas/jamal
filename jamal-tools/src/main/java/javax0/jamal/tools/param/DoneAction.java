package javax0.jamal.tools.param;

public enum DoneAction {
    REQUIRED{
        String toString(String name){
            return " to make it required";
        }
    }, OPTIONAL{
        String toString(String name){
            return " to make it optional";
        }
    }, DEFAULT{
        String toString(String name){
            return " to set the default value";
        }
    }
}
