JAMAL ROADMAP
=============

Rudimentary description of features that are to be developed in the future. The list is not listed in order of priority
and the list is not a promise or guarantee.

- Scripting languages properties can be set to object values if the value is a macro name and the named macro implements
  ObjectHolder.

- Yaml resolve should work if the referenced macro does not hold a Yaml object but some other ObjectHolder.

- Integrate RestAssured so that you can write REST tests in Jamal and converting the document will run the tests and the
  output will that way become a test execution report.

- Implement Yaml data exporting in XML format. No attributes in this case, but that is perfect for XMLs like POM files,
  and it makes possible to use Yaml structural composition for POM files and other XML formats that do not use
  attributes.

- Implement breakpoints in Jamal debugger. A Jamal breakpoint is a string, and the execution in the debugger stops when
  the string is part of the next part to be executed.

- Implement JSON reading and writing and structural construction similar to Yaml.

- Implement XML reading and writing and structural construction similar to Yaml.

- Integrate the MVEL scripting language.

- Module to read data from Microsoft XLS formatted file via Apache POI