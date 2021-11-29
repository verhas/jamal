package javax0.jamal.api;

import java.util.Locale;
import java.util.Optional;

/**
 * This class defined all the strings for the environment variables that Jamal uses in the engine or in the core
 * macros.
 * <p>
 * The symbolic name for the environment variables is always the name and {@code _ENV} prefix.
 * The configuration always looks at the system properties as well.
 * The name of the system property is always a converted string from the environment variable name.
 * The name is lowercase and the {@code _} character is replaced by a {@code .} dot character.
 * For example {@code JAMAL_CONNECT_TIMEOUT} becomes {@code java.connect.timeout}.
 * <p>
 * This file also contains the documentation of the environment variables used by the README.adoc.jam as snippets.
 * Hence, the documentation format in those comments is ASCIIDOC.
 */
public class EnvironmentVariables {
    /*
snippet JAMAL_CONNECT_TIMEOUT_documentation
{%@define E===== %}
{%E%}{%JAMAL_CONNECT_TIMEOUT_ENV%}
This variable can define the connection timeout value for the web download in millisecond as unit.

The default value for the timeouts is 5000, meaning five seconds.

The proxy setting can be configured using standard Java system properties.
For more information see the JavaDoc documentation of the class `java.net.HttpURLConnection` in the JDK documentation.
end snippet
*/
    public static final String JAMAL_CONNECT_TIMEOUT_ENV = "JAMAL_CONNECT_TIMEOUT";
    /*
    snippet JAMAL_READ_TIMEOUT_documentation

{%E%}{%JAMAL_READ_TIMEOUT_ENV%}
This variable can define the read timeout value for the web download in millisecond as unit.

The default value for the timeouts is 5000, meaning five seconds.
end snippet
*/
    public static final String JAMAL_READ_TIMEOUT_ENV = "JAMAL_READ_TIMEOUT";
    /*
snippet JAMAL_TRACE_documentation

{%E%}{%JAMAL_TRACE_ENV%}
This environment variable defines the name of the trace file.
When a trace file is defined the evaluation and all the partial evaluations are appended to this file during processing.
This file can grow very fast, and it is not purged or deleted by Jamal.
end snippet
*/
    public static final String JAMAL_TRACE_ENV = "JAMAL_TRACE";
    /*
snippet JAMAL_STACK_LIMIT_documentation

{%E%}{%JAMAL_STACK_LIMIT_ENV%}

sets the recursive call depth in macro evaluation.
Macros may be recursive and in some cases it may create infinite recursive calls in Jamal.
Try a simple Jamal file that contains `{@define a={a}}{a}`.
This will drive Jama into an infinite recursive call.
During the macro evaluation `{a}` will result `{a}` again and this will be evaluated again and again.
Infinite recursive calls result `StackOverflowError` which should not be caught by any program.
To avoid this Jamal limits the recursive calls to the maximum depth 1000.
This is a reasonable limit.

* Most Jamal sources are not complex, and will not get above this limit recursively.
* At the same time, most Java implementations can handle this dept.

This limit may be too much in your environment.
Jamal may still throw StackOverflowError.
In this case set this to a smaller value.
It may also happen that you deliberately create complex recursive macros.
In that case this limit may be too small.
Set your value to a limit that fits your need.

end snippet
 */
    public static final String JAMAL_STACK_LIMIT_ENV = "JAMAL_STACK_LIMIT";
    /*
snippet JAMAL_CHECKSTATE_documentation

{%E%}{%JAMAL_CHECKSTATE_ENV%}

This environment variable can switch off macro statefulness checking during macro registration.
It is generally recommended that the macros are stateless to support multi-thread evaluation when a single JVM runs multiple Jamal processors in one or more threads.
If a macro has to have a state, it must be annotated using the annotation `Macro.Stateful`.
The statelessness or annotation is checked during macro registering since Jamal version 1.8.0.
You can switch off the functionality setting this environment variable to `false`.
It may be needed if you want to use an older, prior 1.8.0 library or a library that does not follow this rule.
end snippet
*/
    public static final String JAMAL_CHECKSTATE_ENV = "JAMAL_CHECKSTATE";
    /*
snippet JAMAL_DEBUG_documentation

{%E%}{%JAMAL_DEBUG_ENV%}

This environment variable can switch on debugging of Jamal.
To use the debugger this variable has to set to a value, which is recognized by a debugger on the classpath.
The web based debugger recognizes the `http:port` format variables.
Set this variable to `http:8080`, put the `jamal-debug` module on the classpath and after starting Jamal processing open your browser at `http://localhost:8080.
The debugger and the use of it is detailed in a separate section.
end snippet
*/
    public static final String JAMAL_DEBUG_ENV = "JAMAL_DEBUG";
    /*
snippet JAMAL_INCLUDE_DEPTH_documentation

{%E%}{%JAMAL_INCLUDE_DEPTH_ENV%}

This variable can set the maximum number of file include nesting.
The default value is 100.
end snippet
*/
    public static final String JAMAL_INCLUDE_DEPTH_ENV = "JAMAL_INCLUDE_DEPTH";
    /*
snippet JAMAL_HTTPS_CACHE_documentation

{%E%}{%JAMAL_HTTPS_CACHE_ENV%}
end snippet
*/
    public static final String JAMAL_HTTPS_CACHE_ENV = "JAMAL_HTTPS_CACHE";
    /*
snippet JAMAL_DEV_PATH_documentation

{%E%}{%JAMAL_DEV_PATH_ENV%}
This environment variable can define replacements for files.

The aim of this feature is to use a local file during development, and still refer to it using the `https://` URL, which will be the production URL.
You want to run tests without pushing the file to a repository, but at the same time you do not want your code to refer to a dev location to be changed before releasing.

Only absolute file names can be replaced.

For example, you include the file `https://raw.githubusercontent.com/central7/pom/1/pom.jim` in your Jamal file.
You want to replace it with a local file `~/projects/jamal/pom.jim`.
In that case you should set the environment variable

[source]
----
export JAMAL_DEV_PATH=\|https://raw.githubusercontent.com/central7/pom/main/pom.jim?SNAPSHOT=~/github/jamal/pom.jim
----

The environment value is a list of `=` separated pairs.
The list is parsed using the standard `InputHandler.getParts(Input)` method.
This is the reason why the first character in the example is the separator `|`
end snippet
*/
    public static final String JAMAL_DEV_PATH_ENV = "JAMAL_DEV_PATH";

    /*
snippet JAMAL_OPTIONS_documentation

{%E%}{%JAMAL_OPTIONS_ENV%}

This environment variable can define options for the Jama processor.
The value of the variable is interpreted as a multi-part input.
The list is parsed using the standard `InputHandler.getParts(Input)` method.
If you just have one option then you can define that with the name.
If there are multiple options then you have to select a non-alphanumeric separator character and present it in front of the list.

NOTE: that the usual `|` character has a special meaning for the bash, and therefore you may need escaping.
Also note that using `:` as a separator character may work, but it may be misleading as it can also be part of an option name.

The options are set on the top level, there is no need to use a `:` prefix.
To set an option to `false`, you can use the `~` character, but please do not.
Every option default value is `false` when not set.

The typical use of this possibility is to set the option `{%@snip FAIL_FAST /":(.\w+)"/%}`.
This option alters the error processing, and it is more "bound" to the execution than to the document.
It may be a better option to include it in an environment variable, or system property than in the document itself.
Both approaches work.

end snippet
*/
   public static final String JAMAL_OPTIONS_ENV = "JAMAL_OPTIONS";

   /**
    * Converts the environment variable name to the system property name during class initialization.
    *
    * @param s the environment variable name
    * @return the system property name
    */
    private static String env2sys(String s) {
        return s.replace('_', '.').toLowerCase(Locale.ROOT);
    }

    public static Optional<String> getenv(String env) {
        return Optional.ofNullable(System.getProperty(env2sys(env)))
            .or(() -> Optional.ofNullable(System.getenv(env)));
    }

    public static void setenv(String env, String value) {
        System.setProperty(env2sys(env), value);
    }

    public static void resetenv(String env) {
        System.clearProperty(env2sys(env));
    }
}
