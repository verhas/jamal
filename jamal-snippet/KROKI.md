# Using Kroki service

This is a simple demonstration how you can include pictures using the Kroki service into a Markdown file.

You have to import the resource `kroki.jim` into your Markdown file.



The `kroki.jim` file contains macros that can be used to include pictures into a Markdown file.
However, that file also includes macros for Asciidoc.
Both macros are called `kroki`, and have the same parameter.
You should tell which output you would like to use, therefore you should first invoke the macro `use:kroki:markdown`.
This will set the output to Markdown.



Guess, what you can see the Asciidoc version of this file.

After that you can use the `kroki` macro to include pictures.

![vvv](https://kroki.io/plantuml/svg/eNplTz0PgjAQ3fsrLkwwUJXEDUl0d3M3B5ykoS0N1-hg_O8WNFJ1e3fv495xr6zDEQ2MaHsmB8Va8GfZOgWbYhttHDY9dnRSXtNeq84ash40XbwQIzUebacJkiMqm8BdpCYAeVV0y0TKaiL9YDPxiMUHZJrFdQCyGYwbbEgNjhiboSX94yzraj7guVzVVQLIMM1nzyI2g5QV_KW_lbDbBTLuuWDI88B9tVg4OadGT0U4GCfnq_MTL7B2ww==)
