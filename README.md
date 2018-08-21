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

[//]: ( USE SNIPPET */started)
```jam
{@define fruit(color,actualSize,name)=we have an color name of size actualSize}
{fruit/red/apple/20ounce}
{fruit/green/melone/1kg}
```

will be converted by Jamal to the file

[//]: ( USE SNIPPET */started_output)
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

[//]: ( USE SNIPPET */comment)
```jam
this is some {@comment this text
will not appear in the output}text
```
will generate


[//]: ( USE SNIPPET */comment_output)
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
will not be repaced. The actual values get into the body replacing the formal parameters
as they are provided.  

[//]: ( USE SNIPPET */define)
```jam
{@define parameterless=this is a simple macro} macro defined
{parameterless}
{@define withparams(a,b,%66h)=this is a b %66h} macro defined
{withparams/A/more complex/macro}
{withparams/%66h/%66h/zazu} <- %66h is not replaced to zazu in the parameters
```

will generate

[//]: ( USE SNIPPET */define_output)
```jam
 macro defined
this is a simple macro
 macro defined
this is A more complex macro
this is %66h %66h zazu <- %66h is not replaced to zazu in the parameters
```


### `eval`
### `import`
### `include`
### `script`
### `sep`
### `export`
