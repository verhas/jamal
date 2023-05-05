# Using Kroki service

This is a simple demonstration of how you can include pictures using the Kroki service into a Markdown file.

You can import the resource `kroki.jim` into your Markdown file.



The `kroki.jim` file contains macros that to include pictures in a Markdown file.
The file also includes macros for Asciidoc.
Both macros are called `kroki`, and have the same parameter.
The include file cannot know what format you use.
Therefore, you should tell it which output you would like to use.
First invoke the macro `use:kroki:markdown`.
This will set the output to Markdown.



You can guess what macro you could invoke for Asciidoc.

After that, you can use the `kroki` macro to include pictures.

![](https://kroki.io/plantuml/svg/eNplTz0PgjAQ3fsrLkwwUJXEDUl0d3M3B5ykoS0N1-hg_O8WNFJ1e3fv495xr6zDEQ2MaHsmB8Va8GfZOgWbYhttHDY9dnRSXtNeq84ash40XbwQIzUebacJkiMqm8BdpCYAeVV0y0TKaiL9YDPxiMUHZJrFdQCyGYwbbEgNjhiboSX94yzraj7guVzVVQLIMM1nzyI2g5QV_KW_lbDbBTLuuWDI88B9tVg4OadGT0U4GCfnq_MTL7B2ww==)

Using Jamal, you can even use macros inside the picture descriptions.
They will be processed before the picture is rendered.
