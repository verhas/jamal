# jamal

##Just Another Macro Language

Jamal is a text to text processor. It can be used as a templating engine or just to maintain
redundant text files. During development, documentation many times there are text files that
you need to maintain, which contain redundant information.

* There can be some `makefile` or other script or documentation file that differs only
  slightly for the different platforms.
* Some property or other resource files in a Java project may be slightly different for
  the different environments, test, uat, production.
* A textual documentation has cross references but the format does not support automatic
  symbolic anchors and references.
* If you have any other use, please tell us.

Generally Jamal reads a text file and generates another one. In the source file it
processess macros and the result of the macros get in place of the macros. That way
text and macro is mixed in a convenient way.

Macros are delimited with special start and end strings. Jamal does not have any default
for that. The initial setting for these strings is given by the embedding application.
For documentation purposes in the examples we will use `{` as start string and `}` as
end string.

## Simple Example

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

In this sample the built-in macro `define` is used to define a so called user defined macro
`fruit`. This macro has three arguments named `color`, `actualSize` and `name`. When the user
defined macro is used these arguments are replaced with the actual values.

## Other macros

`define` is not the only built-in macro in Jamal. The macros are

* `comment`
* `define`
* `eval`
* `export`
* `import`
* `include`
* `script`
* `sep`, and
* `verbatim`

The built-in macros are used with `#` or `@` in front of the name of the macro. These characters
signal that the macro is not user defined, but rather built in and they also have a side effect.
The usual use is to start a macro with `@` character. In that case the macro is evaluated
with the rest of the input till the closing string (`}` in the samples) as the input is.
If the macro is started with `#` then the input is first parsed for other macros, they are
evaluated their result replacing the occurrences and the macro is evaluated only after this
process. For examples and more detailed explanation about this see the section about the
built-in macro `export`.


### `comment`

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

### `define`

`define` defines a user defined macro in the current scope. Scopes are explained in the section
about `export`. The syntax is

```jam
define id(parameters)=body
```

The parameter part is optional in case there are no parameters for the macro. When the macro is
used the parameters are replaced in the body by the actual values supplied at the place of use.
The parameters are specified comma separated and are usually identifiers.

Note that the parameters do not have any special syntax. The only requirement is that they
do not contain a comma `,`, because the list is comma separated, and a closing parenthesis
`)` because that is the character that terminates the list of the parameters. It is
recommended to use normal identifiers and no spaces in the parameter names.

To ensure that the parameter replacing is consistent and possible to follow
the parameter names can not contain each other as substring. If there was a parameter `a` with
an actual value `oneA` and another `aa` with an actual value `twoAs` then the occurrences 
of `aa` in the body could be replaced to `twoAs` or `oneAoneA`. During the replacement
when an actual value of a parameter contains the same or one or more other parameters these
will not be replaced. The actual values get into the body replacing the formal parameters
as they are provided.

It is possible to use a question mark `?` after the macro keyword `define`. If that is used a
macro is only defined if the macro is NOT yet defined in the current scope or any other
larger scope.

When the name of the macro contains at least one  colon charater `:` then the macro will be
defined in the global scope and thus valid from that point on everywhere in the text. It is
also possible to define a user defined macro to be global without `:` in the name. If the
very first character of the name of the macro is `:` then this character is removed and the
macro is defined in the global scope.

When a user defined macro is used the parameters are defined after the name of the macro. In
case of user defined macros there is no `@` or `#` in front of the name of the macro. Thw
parameters stand after the name of the macro separated by a character. The first
non-whitespace character after the name of the macro is the separator character. It is usually
`/` as in the examples below, but it can be any character that does not appear inside any of
the actual values of the parameters. The number of the parameters should be exactly the same
as the formal parameters.

In the following sample code you can see some examples that demonstrate these.

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

### `eval`

`eval` can be used to execute some script. The syntax of the macro is

```jam
eval/scripttype script
```

If `eval` is followed by `/` character then the next identifier is the type of the script.
If this is missing the default is `JavaScript`. You can use any scripting language that
implements the Java scripting API and the interpreter is available on the classpath when Jamal is
executed.

The following two examples show how eval can be used to evaluate simple arithmetic expressions using thr
Java built-in JavaScript interpreter. Note that in the second example the macro `eval` is preceeded with the
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

### `import`

`import` opens a file and reads the content of the file and interprets it as Jamal macro file.
If there is anything defined in that file it will be imported into the scope of the current
file. If the macro start and end characters are redefined using the `sep` macro it will change
for the file that imported the other file. If there are any user defined macros defined in the
top level scope of the file, they will be available in the  importing file.

On the other hand the output the processing of the file generates will be discarded.

The syntax of the command is

`import file_name`

The name of the file can be absolute or it can be relative to the file that imports the other file.

Use `import` to import user defined macro definitions.

### `include`

`import` reads a file similary to `import` but it starts a new scope for the processing og the
included file. File included cannot redefine the macro starting and ending string and can define
user defined macros for the file including only if the macro is exported from the top level
scope of the included file.

The macro itself is replaced by the output generated by the processing of the included file.

The syntax of the command is

`include file_name`

The name of the file can be absolute or it can be relative to the file that imports the other file.

Use `include` to get the content of a file into the main output.

### `script`

The macro `script` defines a user defined macro that is interpreted as a script. The syntax
of the command is

```jam
script/scripttype id(parameters)=body
```

If `script` is followed by `/` character then the next identifier is the type of the script.
If this is missing the default is `JavaScript`. You can use any scripting language that
implements the Java scripting API and the interpreter is available on the classpath when Jamal is
executed.

The parameters can be used in the script as variables, thus in this case the characters used in
the names of the parameters should be restricted to the characters that can be used in the names
in the scripting language. Usually: normal identifiers. When the user defined macro is used the
global variables of the same name are set with the actual values of the parameters.

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

The output generalted by the Jamal preprocessor:

<!--USE SNIPPET */script_output -->
```jam

1. iterated
2. iterated
3. iterated

```

Note that the JavaScript code itself contains the macro starting and ending characters. This
does not do any harm so long as long these are in pairs, though it is a better practice to 
change the separator characters to something that can not appear in the body of the
user defined macro.


### `sep`

This macro can be used to temporarily change the macro starting and ending script. In the
examples in this documentation we use `{` as starting string and `}` as ending string, but
Jamal itself does not impose 

### `export`
