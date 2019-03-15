# jamal

Just Another Macro Language

Jamal is a text to text processor. It can be used as a templating engine or just to maintain
redundant text files. During development many times there are text files that
you need to maintain, which contain redundant information.

* Some property or other resource files in a Java project may be slightly different for
  the different environments, test support, uat, production.
* A textual documentation has cross references but the format does not support automatic
  symbolic anchors and references.
* There can be some makefile or other script or documentation file that differs only
  slightly for the different platforms.
* If you have any other use, please tell us.

Generally, Jamal reads a text and generates another one. In the source file, it
processes macros and the result of the macros get in place of the macros. That way
text and macro is mixed in a convenient way.

You can use Jamal as a maven plugin, it is also embedded into the Java::Geci code generator
or you can use it as an embeddable macro engine in your Java application.

In this readme, we first discuss how the macros look like and how will Jamal convert its input
to the output, and then we discuss the API that lets you embed the macro processing into your
application.

## Table of contents

1. [Starting Jamal](#Starting)
1. [Simple Example](#SimpleExample)
1. [Other Macros](#OtherMacros)
     1. [`comment`](#comment)
     1. [`block`](#block)
     1. [`begin` and `end`](#begin)
     1. [`define`](#define)
     1. [`eval`](#eval)
     1. [`export`](#export)
     1. [`import`](#import)
     1. [`include`](#include)
     1. [`script`](#script)
     1. [`sep`](#sep)
     1. [`for`](#for)
     1. [`if`](#if)
     1. [`ident`](#ident)
     1. [`verbatim`](#verbatim)
     1. [`options`](#options)
1. [Jamal API](#JamalAPI)

## Starting Jamal<a name="Starting">

The simplest way to start Jamal is to use the Maven plugin. To do that you have to have Maven installed, but
as a Java developer, you probably have. Then you can issue the command

```
mvn com.javax0.jamal:jamal-maven-plugin:1.0.2:jamal
```

if you have a `pom.xml` file in your directory. If you do not then read the documentation of the Jamal Maven plugin
at https://github.com/verhas/jamal/blob/master/jamal-maven-plugin/README.md It is simple and short.

When something goes wrong then Jamal will give you a detailed error message with the file name, line number and
character count where the error happened. In other cases Jamal may think it works fine, but the output is not exactly
what you expected. Sorry, in this case the issue, most probably, is with your expectations. In cases like that you can
specify `-Djamal.trace=tracefile.xml` on the command line that starts Jamal specifying a trace file, in this case
`tracefile.xml`. The tracefile will contain all the
macro evaluations inputs and outputs. Since there can be many Jamal evaluations one after the other, Jamal does not
overwrite old trace information but rather it appends the information. Before starting Jamal you can manually
delete the trace file. Trace files grow large easily. 
  
If you Java 8, 9 or 10 you should go with the release `1.0.2.JDK8`.

## Simple Example<a name="SimpleExample">

As a quick sample to have a jump start what Jamal can do:

<!-- USE SNIPPET */started -->
```jam
{@define fruit(color,actualSize,name)=we have an color name of size actualSize}
{fruit/red/apple/20ounce}
{fruit/green/melone/1kg}
```

will be converted by Jamal to the file

<!-- USE SNIPPET */started_output -->
```jam

we have an red 20ounce of size apple
we have an green 1kg of size melone
```

In this sample, the built-in macro `define` is used to define a so-called user-defined macro
`fruit`. This macro has three arguments named `color`, `actualSize` and `name`.
When the user-defined macro is used these arguments are replaced with the actual values.

Note that in this example the macros open with the `{` character and close with the `}` character.
This is not hardwired in Jamal and there is not even a suggested default for that. The embedding
application has to define the opening string and the closing string. For example the embedding
Java::Geci application uses `{{` and `}}` as macro open and macro close strings because in that
textual environment the `{` and `}` characters are frequently used in Java code, but rarely in
double `{{` or `}}` format. In this documentation, we use the `{` and `}` strings. You can change
the macro opening and closing strings even from macros using the built-in `sep` macro (see later).

When these macros are used the parameters are separated using the first non-space character that is following
the name of the macro. Thus you can write

```jam
{fruit/red/apple/20ounce}
{fruit|red|apple|20ounce}
{fruit.red.apple.20ounce}
{fruit :red:apple:20ounce}
```

Note that in the last example we used the `:` character as the separator. Since this character can also be part of the
name of the macro (in this case the macro is global) this cannot be used without a space in front of the first `:`
character.

## Other Macros<a name="OtherMacros">

`define` is not the only built-in macro in Jamal. The comprehensive list of built-in macros are

* `comment`, `block`
* `begin` and `end`
* `define`
* `eval`
* `export`
* `import`
* `include`
* `script`
* `sep`
* `for`
* `use`
* `if`
* `ident`
* `verbatim`, and
* `options`

The built-in macros are used with `#` or `@` in front of the name of the macro. These characters
signal that the macro is not user-defined but rather built in and they also have a side effect.
The usual use is to start a macro with the `@` character. In that case, the macro is evaluated
with the rest of the input till the closing string (which is `}` in the samples).
If the macro starts with the `#` character then the input is first parsed for other macros, they are
evaluated their result replacing the occurrences and the macro is evaluated only after this
process. For examples and more detailed explanation about this see the section about the
built-in macro `export`.


### `comment`<a name="comment">
since 1.0.0 (core)

`comment` is used to insert comments to the input, and it can also be used to enclose
definitions without side effects. For more about definition scopes and exporting read
the section about `export`.

<!-- USE SNIPPET */comment -->
```jam
this is some {@comment this text
will not appear in the output}text
```
will generate


<!-- USE SNIPPET */comment_output -->
```jam
this is some text
```

### `block`<a name="block">
since 1.0.0 (core)

`block` is technically exactly the same as `comment`. It is recommended to use the `comment` macro
with the `@` starting character so that the content of the comment is not interpreted by Jamal and
to use `block` with `#` to have the content interpreted. Block should be used to enclose
definitions to a scope level.

For more about definition scopes and exporting read the section about `export`.

### `begin`<a name="begin"> and `end`
since 1.0.0 (core)

The macros `begin` and `end` can be used to start and to finish a local definition scope. The
effect is practically the same as having the text between the `begin` and `end` macro inside
a `#block` macro (note the starting `#` character).

It is recommended to use `begin` and `end` when the structure is complex and it is
more readable to use the `begin` / `end` macros than a simple `block`. To ensure that
all `begin` has an `end` you can name the blocks. You can put an arbitrary string after the
macro name `begin` and if you do then you have to repeat the same string after the macro name `end`
(spaces are trimmed).

 ```jam
 {@define Z=1}
 {@begin alma}
    {@define Z=2}{Z}
    {@define S=2}{@export S}
 {@end alma }{Z}{S}
 ```

Scopes are opened by many things, like macro start, including a file. You can close a scope
using the macro `end` that was opened with a matching `begin`. You cannot and should not
close a scope using `end` that was opened by something else. For
example, you can not get into the scope of the including class putting a pairless `end` macro
into an included file. This will trigger a processing error.

### `define`<a name="define">
since 1.0.0 (core)

`define` defines a user-defined macro in the current scope. Scopes are explained in the section
about `export`. The syntax is

```jam
define id(parameters)=body
```

The parameters part is optional in case there are no parameters for the macro. When the macro is
used the parameters are replaced in the body by the actual values supplied at the place of use.
The parameters are specified comma separated and are usually identifiers.

Note that the parameters do not have any special syntax. The only requirement is that they
do not contain a comma `,`, because the list is comma separated, and a closing parenthesis
`)` because that is the character that terminates the list of the parameters. It is
recommended, though, to use normal identifiers and no spaces in the parameter names, but
this is only a recommendation and is not enforced by Jamal, because you may need to process some
special text the developer could not imagine and you may need some specially named parameters.

Somebody may follow other conventions, like starting every parameter with the `$` or
enclosing the parameter names between `|` or `/` or some other characters. These
practices can be absolutely okay so long as long they support the readability of the
macro body and the use of the macro. Applying such practices may help to visually
separate the macro parameters from the textual content of the macro body.

To ensure that the parameter replacing is consistent and possible to follow
the parameter names cannot contain each other as a substring. If there was a parameter `a` with
an actual value `oneA` and another `aa` with an actual value `twoAs` then the
occurrences of `aa` in the body could be replaced to `twoAs` or `oneAoneA`. During the replacement
when an actual value of a parameter contains the same or one or more other parameters these
will not be replaced. The actual values get into the body replacing the formal parameters
as they are provided.

It is possible to use a question mark `?` after the macro keyword `define`. If that is used a
macro is only defined if the macro is NOT yet defined in the current scope or any other
larger scope.

When the name of the macro contains at least one colon character `:` then the macro will be
defined in the global scope and thus valid from that point on everywhere in the text. It is
also possible to define a user-defined macro to be global without `:` in the name. If the
very first character of the name of the macro is `:` then this character is removed and the
macro is defined in the global scope.

When a user-defined macro is used the parameters are defined after the name of the macro. In
case of user-defined macros, there is no `@` or `#` in front of the name of the macro. Optionally
there may be a `?` character. In that case the result of an undefined user macro will be
the empty string. Any other use of an undefined user macro results error. The
parameters stand after the name of the macro separated by a character. The first
non-whitespace character after the name of the macro is the separator character. It is usually
`/` as in the examples below, but it can be any character that does not appear inside any of
the actual values of the parameters. The number of parameters should be exactly the same
as the formal parameters.

In the following sample code, you can see some examples that demonstrate these.

<!-- USE SNIPPET */define -->
```jam
{@define parameterless=this is a simple macro} macro defined
{parameterless}
{@define withparams(a,b,%66h)=this is a b %66h} macro defined
{withparams/A/more complex/macro}
{withparams/%66h/%66h/zazu} <- %66h is not replaced to zazu in the parameters
{@define? withparams(a,b,c)=abc}here 'withparams' is not redefined
{withparams|a|b|c}
{#comment {@define x=local}{@define :x=global} {#define :y=here we are {x}}}
{y}
here we are {x}
```

will generate

<!-- USE SNIPPET */define_output -->
```jam
 macro defined
this is a simple macro
 macro defined
this is A more complex macro
this is %66h %66h zazu <- %66h is not replaced to zazu in the parameters
here 'withparams' is not redefined
this is a b c

here we are local
here we are global
```

This is a fairly complex example. To ease the understanding note the followings:

1. `%66h` is an absolutely valid marco parameter name
1. When a macro parameter is replaced in the body of the macro the processing
   of that string is finished and is not processed further replacing macro
   parameters. Macro parameters are only replaced with the actual values in the
   macro body and not in the parameter actual values. That is why parameters
   `a` and `b` are replaced with the actual string '%66h' but then this is not
   replaced with the actual value of the parameter `%66h`.
1. When we define the macros `x` and `y` inside the `comment` macro it happens in
   a local scope of the `comment` macro. it means that the definition of `x` has
   no effect outside the macro `comment`. Using the name `:x` defines the macro
   `x` in the global scope, that is above the current scope. When we defined the
   macro `y` it also starts with `:` and so it gets into the global scope. However,
   during the definition, it is in the local scope of the `comment` macro where the
   local definition of `x` overrides the global definition of `x` even though the
   global definition happened later. Therefore `y` will be `here we are local`.
   That is also because  `y` is defined using the `#` character before the
   built-in macro keyword `define` and thus the content of the definition is
   evaluated before defining the global `y`.

### `eval`<a name="eval">
since 1.0.0 (core)

`eval` interprets the content of the macro, the text written after the macro keyword `eval` using the syntax defined
as script type after a `/` character. If there is no script type defined (or `jamal` is defined) then the content will
be evaluated as normal Jamal macro text. Otherwise, the script engine named is used.  

The syntax of the macro is

```jam
eval macro text
```

or

```jam
eval/scripttype script
```

If `eval` is followed by `/` character then the next identifier is the type of the script.
You can use any scripting language that
implements the Java scripting API and the interpreter is available on the classpath when Jamal is
executed.

The following two examples show how `eval` can be used to evaluate simple arithmetic expressions using the
Java built-in JavaScript interpreter. Note that in the second example the macro `eval` is preceded with the
character `#` therefore the body of the macro is parsed for other macros before `eval` itself is invoked.
That way `{a}` and `{b}` are replaced with their defined values and what eval sees is `1+2`.

<!-- USE SNIPPET */eval -->
```jam
{@eval/JavaScript 1+3}
{@define a=1}{@define b=2}
{#eval {a}+{b}}
```

<!-- USE SNIPPET */eval_output -->
```jam
4

3
```

### `import`<a name="import">
since 1.0.0 (core)

`import` opens a file and reads the content of the file and interprets it as Jamal macro file.
If there is anything defined in that file it will be imported into the scope of the current
file. If the macro opening and closing strings are redefined using the `sep` macro it will change
for the file that imported the other file. If there are any user-defined macros defined in the
top-level scope of the file, they will be available in the importing file.

(Note that top-level scope of the file may not be the same as the global scope. If the importing
happens from an included file, or from inside a block of from inside a macro, or in a scope that
was started with a `begin` macro then the "top-level-scope of the file" is the one
that contains the `import` macro. If anything is defined into the global scope in the imported
file then those macros will eventually be in the global scope and available to anyone later.)

On the other hand, the output that the processing of the file generates will be discarded.

The syntax of the command is

`import file_name`

The name of the file can be absolute or it can be relative to the file that imports the other file.

Use `import` to import user defined macro definitions.

Because the textual output from the evaluation of the file is discarded feel free to use text
in the file to be imported as documentation.

### `include`<a name="include">
since 1.0.0 (core)

`include` reads a file similarly to `import` but it starts a new scope for the processing of the
included file. The file included cannot redefine the macro opening and closing string and can define
user-defined macros for the file including only if the macro is exported from the top level
scope of the included file.

The macro itself is replaced by the output generated by the processing of the included file.

The syntax of the command is

`include file_name`

The name of the file can be absolute or it can be relative to the file that imports the other file.

Use `include` to get the content of a file into the main output.

### `script`<a name="script">
since 1.0.0 (core)

The macro `script` defines a user-defined macro that is interpreted as a script. The syntax
of the command is

```jam
script/scripttype id(parameters)=body
```

If `script` is followed by `/` character then the next identifier is the type of the script.
If this is missing the default, `JavaScript` is assumed. You can use any scripting language that
implements the Java scripting API and the interpreter is available on the classpath when Jamal is
executed.

The parameters are handled differently from the parameters of the user-defined macros defined
using the `define` built-in macro. In that case, the parameter strings are replaced by the actual
value strings during evaluation. In this case, the parameters are used as global variable names and
using these names the actual values are injected into the context of the script before evaluation.

This also implies that you do not have the total freedom of parameter names that you had for
user-defined macros defined using the built-in macro `define` (note that you could use there any string
as a parameter id so long as long it contained no comma and closing parenthesis). In this case, you
should care about the syntax of the scripting language used. The parameter names have to be valid
identifiers in the scripting language as they are used as such.

The value injection converts the actual value of the parameter to script values. Because in this case
the values are not injected into the macro body as string replacement but rather assigned to
global variables in the script some conversion should take place. Without this, all the scripts that
use some integer or floating point number were supposed to convert them first from the string.

Therefore Jamal tries to convert the actual value of a parameter of a `script` defined user-defined
macro treating it as an integer. If it succeeds then the global variable having the name as the parameter
will hold an integer value (or whatever the scripting language uses from the Java scripting context
injected as `Integer`). If the conversion to an integer does not work then it tries the same with double.
If that is also not feasible then it will check is the actual value is lower case `true` or `false` in
which case the global variable of the script will be a boolean value. In any other case, the global
variable will get the actual value as a string assigned to it.

The following sample shows a simple script that implements a looping construct using JavaScript.
The source Jamal file:

<!--USE SNIPPET */script -->
```jam
{@script for(loopvar,start,end,content)=
    c = ""
    for( i = start ; i <= end ; i++ ){
      c = c + content.replace(new RegExp( loopvar , 'g'), i)
    }
    c
}
{for%xxx%1%3%xxx. iterated
}
```

The output generated by the Jamal preprocessor:

<!--USE SNIPPET */script_output -->
```jam

1. iterated
2. iterated
3. iterated

```

Note that the JavaScript code itself contains the macro opening and closing characters. This
does not do any harm so long as long these are in pairs, though it is a better practice to 
change the separator characters to something that can not appear in the body of the
user-defined macro.

### `for`<a name="for">

The macro `for` can be used to repeat the same text many times. The syntax of the macro is

```jam
{@for variable in (a,b,c,d)= content to be repeated}
```
 
The `variable` can be used in the content and will be replaced for each iteration with the respective
element on the comma separated list. The list can also be separated by other strings. If the macro `$forsep`
is defined, like in

```jam
{@define $forsep=\s+}
```

then the arguments will be separated by one or more spaces. The string between the `(` and the `)` will be split
using the string defined in `$forsep` as a regular expression.

Later versions may extend the command with other, more complex syntax.
 
### `if`<a name="if">

The `if` macro makes it possible to evaluate the content conditionally. The syntax of the macro is:

```jam
{#if/test/then content/else content}
```

Here we use `/` as separator character, but any other character can be used that does not appear in the `test` text
and in the `then content` text. The first non-space character following the macro keyword `if` will be used as a separator
character.

The result of the evaluated macro will be the `then content` when the `test` is true and the
`else` content otherwise. The `test` is true, if 

* it is the literal `"true"` (case insensitive),
* an integer number and the value is not zero or 
* any other string that contains at least one non-space character, except 
* when the `test` is the literal `"false"` (case insensitive) then the test is false.


### `ident`<a name="ident">
since 1.0.0 (core)

`ident` is an extremely simple macro. It just returns the body of the macro. The name 
stands for _identity_. It is useful to use when some macro should not be evaluated but
others should during the definition of a user-defined macro. For example:

<!--USE SNIPPET */ident -->
```jam
{@define b=92}{#define c={@ident {a}}{b}}{@define a=14}{c}
```

When we define the macro `c` we do not want to evaluate `{a}` because at that point `a` is not defined, but
we do want to evaluate `b`. This way `c` will become `{a}92`. When later `c` is used and `a` is already defined then
the final result will be `1492`. 

<!--USE SNIPPET */ident_output -->
```jam
1492
```

Note that `c` is defined using the `#` character in front of `define` and we used `@` in `ident`.

If we redefine later `a` to some different value then `c` will follow this change, but if we redefine only `b`
the value of `c` will still remain `1492`.

### `verbatim`<a name="verbatim">
since 1.0.0 (core)

`verbatim` is a special macro that affects macro evaluation order and is used for advanced
macro evaluation. To understand what it does we have to discuss first how Jamal evaluates
the different macros.

Jamal parses the input from the start towards the end and copies the characters from the
input to the output. Whenever it sees a macro then it evaluates the macro and the result of
the evaluation is copied to the output. This evaluation is done in three steps, two of
those are recursive. Let's have a simple example:

<!--USE SNIPPET */verbatim1 -->
```jam
{@define a=this is it}{@define b={a}}{#define c={b}}{c}
```

The macro `a` is defined simply. It is `this is it`. Whenever `a` is evaluated it will result
the string `this is it`.

The macro `b` has the value `{a}`. When `b` is evaluated it results `{a}` and then before
using this output in place of the use of the macro `b` this result is evaluated by Jamal as
a new input. If the macro opening and closing strings are still `{` and `}` at this time, then
this second recursive evaluation will result in the string `this is it`.

The macro `c` is defined using the `#` character at the start of the macro, therefore Jamal
will process the body of the macro before processing the built-in macro `define` itself.
Essentially it will evaluate `{b}`, put the resulting characters after the `=` sign
in the definition of `c` and then evaluate the `define`.

As we discussed above when this time `{b}` is evaluated it results `{a}`, which also gets evaluated
and then it results `this is it`. Therefore the value of the macro `c` is `this is it` and
that is what we see in the output:

<!--USE SNIPPET */verbatim1_output -->
```jam
this is it
```

This way the evaluation of a macro is done in three steps:

1. Evaluate the body of the macro unless the macro is built-in and starts with the character `@`.
   When evaluating the macros in the body of the macro starts a new scope and evaluate the macros
   following these three steps.
2. Evaluate the macro itself.
3. If the macro is user-defined then evaluate the output of the macro and if it contains
   macros then evaluate those using these three steps.

As you can see the first and the last steps are recursive steps. The first step can be skipped
using the `@` character. The second step cannot be skipped, and after all, there is no reason to
do so. In the case of user-defined macro, however, the third step can be skipped using the macro `verbatim`.

The syntax of the `verbatim` macro is the following:

```jam
verbatim userDefinedMacroUse
```

The `verbatim` macro has to be followed by a user defined macro usage. If we modify the previous
example to use `verbatim` we can do it the following way:

<!--USE SNIPPET */verbatim2 -->
```jam
{@define a=this is it}{@define b={a}}{#define c={@verbatim b}}{c} {@verbatim c}
```

In this example `{@verbatim b}` is the same as `{b}` in the previous example with the
exception that after `b` is evaluated the result is not processed further for macros
but it is used directly (verbatim) as the value of the new macro `c`. Also when we
use `{c}` the result of `c` is scanned as a third step for further macros and indeed, 
in this case, there is one, because the value of the macro `c` is `{a}` in this case, that further
evaluates to `this is it`. On the other hand when we use `{@verbatim c}` then the
result `{a}` is not processed any further.

<!--USE SNIPPET */verbatim2_output -->
```jam
this is it {a}
```
Note that the macro `verbatim` is a special one because it is hardwired into the evaluation logic of
Jamal and it is not a "real" built-in macro. In other words, if there are user-defined macros and
built-in macros then `verbatim` is one level deeper built-in than the other built-in macros. To
understand this may be important if you want to write your own built-in macros as Java classes.
You cannot "redefine" `verbatim`.

### `sep`<a name="sep">
since 1.0.0 (core)

This macro can be used to temporarily change the macro opening and closing string. In the
examples in this documentation we use `{` as the opening string and `}` as the closing string, but
Jamal itself does not impose any such predefined setting.

The syntax of the command is

```jam
sep /startString/endString
```

There can be whitespace characters after the macro name `sep`, but these are optional. The first
non-space character is used as a separator character that separates the macro opening string from
the macro closing string. It is usually the `/` character, but it can be anything that does not
appear in the opening string. This generally the syntax of the macro is

```jam
sep \s* (\S) opening_string (\1) closing_string 
```

Note that the macro `sep` should be terminated with the original macro closing string, but the
macros after it already have to use the altered opening and closing strings.

The change of the opening and the closing strings always happens in pairs. You cannot change only
the closing or only the opening string. You can, however, redefined one of them to be something
that is different from the current value and the other one to be the same as the current value.
Even in this case, the definition should specify both strings. The change is valid only
for the current scope and the original value is restored when returning from the scope, even if the
opening and closing strings were set to different values multiple times.

Neither the opening nor the closing string can be empty. Trying to set it to an empty string
will raise an error. This usually happens when you get used to the `/` separator character
as a convention and you forget to put it in front of the opening string, like in `{@sep [/]}`.
(Jamal v1.0.0 gets into an infinite loop in case of an empty opening string. Later versions will
signal an error.)

When the opening and the end strings are set the original values are stored in a list. When the
macro `sep` is used without any separator character, in other words, it is nothing more than the
`sep` macro name, like `{@sep}` then the last opening and closing strings are restored.

The following sample is executed with `{` and `}` as opening and closing string at the beginning.
After that, it sets the strings to `[[` and `]]`. This is used to define the macro `apple`. After
this when the scope of the next macro, `comment` starts the opening and closing strings are still `[[`
and `]]`.  Starting a new scope does not change the macro opening and closing strings.

Note, however, that it would be an error to use `[[@sep]]` inside the scope of the macro
`comment` at this point trying to restore the original macro opening and closing strings.
In that scope at the start, there are no opening and closing strings to be
restored. The opening and closing strings do not belong to this scope, they are simply inherited
from the outer scope. On the other hand, the sample can change the strings, as it does to
`<<` and `>>`. Using these it defines the macro `z`. Note that `z` is not exported from this
scope.

After that the `<<@sep>>` restores the opening and closing strings to the inherited one and with these,
it defines `a1` and `a2` and also exports them. Note, that `a1` will have the actual value of
the macro `z` evaluated inside the scope of the `comment` macro. The macro `a2` starts with `@`
thus the body is not parsed during the macro definition and thus the value of `a2` is `[[z]]`
unevaluated, as it is. Similarly, the macro `a3` will have the value`{z}`.

All these macros are evaluated because the macro `comment` is started with the character
`#` which means that Jamal will evaluate the body of the macro before evaluating the
macro itself.

After the `comment` macro the separators are set back to the original value `{` and `}`
automatically. Then we have a simple macro definition that defines `z` and then
this `z` is used, and also the exported `a1`, `a2` and `a3`.

`z` is now, as defined in the outer scope is `SSS`. `a1` has the value that came from the
macro `z` as it was defined inside the scope of the macro `comment`. Macro `a2` has the
value `[[z]]` that has nothing special in the current scope. The macro `a3` has the value
`{z}` which is evaluated after the macro `a3` is replaced with its value and 

<!--USE SNIPPET */sep -->
```jam
{@sep/[[/]]}
[[@define apple=fruit]]
[[apple]]
[[#comment [[@sep/<</>>]]
<<@define z=zazi>>
<<#sep>>
[[#define a1=[[z]]]]
[[@define a2=[[z]]]]
[[@define a3={z}]]
[[@export a1,a2,a3]]
]]
[[@sep]]
{@define z=SSS}
{z}{a1}{a2}{a3}{@verbatim a3}
```
<!--USE SNIPPET */sep_output -->
```jam


fruit



SSSzazi[[z]]SSS{z}
```

### `export`<a name="export">
since 1.0.0 (core)

`export` moves the definition of one or more user-defined macros to a higher scope. When a macro is defined it
is defined in the current scope (unless the name contains or starts with `:`).

The file that Jamal is processing is one scope and if there is a macro defined
in the file on the top level than that macro can be used anywhere inside that file. However, when Jamal includes
a file into another it opens a new scope. The macro `include` should include some text in the output. It can
be used, for example, to split up a long document into chapters and then use Jamal to create the final output.
In that case, the macros defined in the included files should not interfere with the definitions in the file that
includes the other one. To accomplish this separation when Jamal includes a file it starts a new scope. Scopes
are embedded into each other like a call stack in programming languages. When a macro is defined in a scope
it is available in that scope and all other scopes that are opened from that scope. When a macro is redefined in
a scope the redefined value is used until the point the scope is closed. In case of an included file, the
user-defined macros defined in the included file disappear as soon as the included file processing is finished.

The setting and resetting of the separator characters is also limited to the scope where the setting is and you
cannot reset the separator character to a value that was set in a lower scope or higher scope.

Jamal opens a new scope in the following cases:

* When a file is processed with the `include` macro.
* When macros are evaluated inside another macro. This is the case of user-defined macros or in case of built-in
  macros when they are started with the character `#`.
* Other built-in macros that are not part of the core package may also start and close scopes. Note that
  built-in macros can be provided in form of JAR files.

Note that the macro `import` does NOT open a new scope to process the imported file. This is because of the aim of
`import` is to have the macros defined in the imported file available in the file that imports them.

In the following example, we define the macro `Z` in the scope of the macro `comment`. The `{@define Z=13}`
is evaluated before the `comment` macro because we use the `#` in front of the `comment` macro. When the
`comment` is evaluated the scope is closed and `Z` is not defined anymore. In the second case the macro
`Z` is exported using the `export` macro. The `export` macro moves the definition of the macro from the scope
of the `comment` to the enclosing scope.
  
The example:  
  
<!--USE SNIPPET */export -->
```jam
A comment starts a new scope {#comment {@define Z=13}} Z {?Z} is not defined here unless...
{#comment {@define Z=14}{@export Z}}Z is exported. In that case Z is {Z}.
```

will result:

<!--USE SNIPPET */export_output -->
```jam
A comment starts a new scope  Z  is not defined here unless...
Z is exported. In that case Z is 14.
```

You cannot export a macro that is defined in a higher scope. You can use those macros and you can reference them
but you can not export them to the enclosing scope because they do not belong to the current scope. You cannot
export macros from the top level scope, because in that case there is no enclosing scope.

### `options`<a name="options">
since 1.0.3 (core)

The options macro can be used to alter the behavior of Jamal. The options can be listed `|` separated as an argument
to the macro. The macro does not check the options name. It stores the options and it can be queried by any other
built-in macro. The scope of the options is local the same way as the scope of user defined macros. Technically the
options are stored in a user defined macro that has the name <tt>`options</tt> and it is possible to export this
macro to higher layers. (Note that the name starts with a backtick.)

```jam
{@define macro(a,b,c)=a is a, b is b{#if :c:, c is c}}{macro :apple:pie:}{@comment 
here we need : at end, default is not lenient}
{#ident {@options lenient}{macro :apple:pie}}
{macro :apple:pie:}{@comment here we must have the trailing : because options is local}
{#ident {@options lenient}{macro :apple:pie}{@export `options}}
{macro :apple:pie}
```

The options implemented currently:

#### `lenient`

In lenient mode the number of the arguments to a user defined macro do not need to be exactly the same as it is
defined. If there are less values provided then the rest of the arguments will be empty string in lenient mode.
Similarly, if there are more arguments than needed the extra arguments will be ignored. 

## Jamal API<a name="JamalAPI">

Embedding Jamal into an application is very simple. You need the Jamal libraries on
your classpath. If you use maven you can simply have

```xml
        <dependency>
            <groupId>com.javax0.jamal</groupId>
            <artifactId>jamal-engine</artifactId>
            <version>1.0.2</version>
        </dependency>
```

in your pom file. The library `jamal-engine` transitively depends on the other libraries that
are needed (`jamal-core`, `jamal-api` and `jamal-tools`).

You also have to specify that you use these modules (Java 9 and later) and that you

```java
module jamal.maven {
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
}
```

The code that invokes Jamal needs a processor that will process the input.

```java
var processor = new Processor(macroOpen, macroClose);
var result = processor.process(input);
```

The `macroOpen` and `macroClose` parameters are `String` values. The parameter `input` to the method
`process()` has to be an object that implements the `javax0.jamal.api.Input` interface. The
easiest way to do that is to use the readily available class `javax0.jamal.tools.Input`
that implements this interface.

You can see an example to create an `Input` from an existing file in the `jamal-maven-plugin`
module. The method `createInput()` reads a file and then using the name of the file and the
content of the file it creates a new input:

```java
private Input createInput(Path inputFile) throws IOException {
    var fileContent = Files.lines(inputFile).collect(Collectors.joining("\n"));
    return new javax0.jamal.tools.Input(fileContent, new Position(inputFile.toString(), 1));
}
```

An `Input` holds the content that the processor has to process but it also has a reference file name
used to resolve the absolute names of the included and imported files and it also keeps track of the
line number and the column of the actual character as the macro evaluation progresses. A `new Position(s,1)`
creates a new position that identifies the file by the name `s` and the line number 1.

When a new processor is instantiated it uses the `ServiceLoader` mechanism to find all the
built-in macros that are on the classpath. If your application has special macros
implemented in Java then you can just put the library on the modulepath and in case the
classes are defined in the `provides` directive of the module then Jamal will find and load
them automatically.

It is also possible to define user-defined and built-in macros via API. To do that you need
access to the `MacroRegister` object that the `Processor` object has. To get that you
can invoke the method `getRegister()` on the processor object:

```java
var register = processor.getRegister();
```

The register has API to define macros and user-defined macros. For further information
see the API JavaDoc documentation.
