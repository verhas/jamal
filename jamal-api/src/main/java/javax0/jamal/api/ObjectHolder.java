package javax0.jamal.api;

/**
 * A class is an object holder if it contains an object (reference), that can be queried calling the method {@link
 * #getObject()}.
 * <p>
 * This interface is implemented by classes in extension modules that also implement the {@link UserDefinedMacro}
 * interface, and implement a functionality that can give a raw object when integrated with other built-in macros or
 * user defined macro implementations that can use the raw object embedded into the "macro".
 * <p>
 * An example is the {@code YamlObject} in the {@code jamal-yaml} module. When a Yaml formatted file is processed via
 * the built-in macro {@code yaml:define} then the created object is an instance of a class that implements {@link
 * UserDefinedMacro} and this interface. The object contains the data structure, which was built from the Yaml data
 * description. Other built-in macros or classes implementing the interface {@link UserDefinedMacro} may use the method
 * {@link #getObject()} when they work with some macro that implements this interface.
 * <p>
 * The typical use is, when a Jamal embedded scripting engine can get access to these structures.
 */
public interface ObjectHolder {
    Object getObject();
}
