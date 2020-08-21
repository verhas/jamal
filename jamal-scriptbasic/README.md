# Jamal ScriptBasic integration module

```xml
<dependency>
    <groupId>com.javax0.jamal</groupId>
    <artifactId>jamal-scriptbasic</artifactId>
    <version>1.0.0</version>
</dependency>
```

Put this dependency on the classpath of Jamal and

```
{@import res:javax0/jamal/scriptbasic/scriptbasic.jim}
```

in your Jamal file. It will let you

```
{expr 13+14*55}
```

evaluate any expression that would be valid in a ScriptBasic program and

```
{basic for i=1 to 13
if i%2 = 1 then
  oddity = "odd"
else
  oddity = "even"
endif
print i,". is an ",oddity," number\n"
next
}
```

to execute a whole BASIC program and get the output into the Jamal output.

Note that you can also mix Jamal into the BASIC, like

```
{@define start=1}{@define end=13}

{basic for i={start} to {end}
if i%2 = 1 then
  oddity = "odd"
else
  oddity = "even"
endif
print i,". is an ",oddity," number\n"
next
}
```
 
because the BASIC interpreter gets the code only after the macro input was already evaluated.
In the example `{start}` and `{end}` get replaced by `1` and `13`.