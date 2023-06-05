# Sample documentation





.cached
![Mermaid Diagram](images/e8a96c341e35bf3594a44f6a47e198b17c60affc393db847a4e26f7ed05708b5.svg)




## 1. Dependency

```xml
<groupId>com.javax0.jamal</groupId>
<artifactId>jamal-test</artifactId>
<version>2.2.0-SNAPSHOT</version>
```





## 2. Array Sample



```java
01 public class Array implements Macro {
02     @Override
03     public String evaluate(Input in, Processor processor) throws BadSyntax {
04         final var pos = in.getPosition();
05         final String[] parts = InputHandler.getParts(in);
06         BadSyntaxAt.when(parts.length < 2, "Macro Array needs an index and at least one element", pos);
07         final int size = parts.length - 1;
08         final int index;
09         try {
10             index = Integer.parseInt(parts[0]);
11         } catch (NumberFormatException nfe) {
12             throw new BadSyntaxAt("The index in Macro array '"
13                     + parts[0]
14                     + "' cannot be interpreted as an integer.", pos, nfe);
15         }
16         BadSyntaxAt.when(index < 0 || index >= parts.length - 1, "The index in Macro array is '"
17                 + parts[0]
18                 + "' but it should be between "
19                 + (-size) + " and " + (size - 1) + ".", pos);
20         return parts[index + 1];
21     }
22 }

```
[snippet](/Users/verhasp/github/jamal/jamal-test/src/main/java/javax0/jamal/test/examples/Array.java)

## 3. Hello World Sample


```java
01 package javax0.jamal.test.examples;
02 
03 import javax0.jamal.api.Input;
04 import javax0.jamal.api.Macro;
05 import javax0.jamal.api.Processor;
06 
07 public class HelloWorld implements Macro {
08     @Override
09     public String evaluate(Input in, Processor processor) {
10         return "Hello, World!";
11     }
12 }

```
[snippet](/Users/verhasp/github/jamal/jamal-test/src/main/java/javax0/jamal/test/examples/HelloWorld.java)
The array chapter is 2. 

## 4.  Simple UML

![world's simplest uml](sample.svg)
