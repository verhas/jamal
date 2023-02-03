[WARNING]
--
* Macro was not terminated in the file.
* @define HEADER($x)=$x
*  at /Users/verhasp/github/jamal/jamal-test/TEST_PLAYGROUND.md.jam/8:3
--
# H1
## H2
### H3
#### H4
##### H5
###### H6

Alternatively, for H1 and H2, an underline-ish style:
[WARNING]
--
* Macro was not terminated in the file.
* @define HEADER($x)=$x
*  at /Users/verhasp/github/jamal/jamal-test/TEST_PLAYGROUND.md.jam/8:3
--
{%@define HEADER($x)=$x
{%
======
%}\
,mn
{%HEADER Alt-H1%}

Alt-H2
------
Emphasis, aka italics, with *asterisks* or _underscores_.

Strong emphasis, aka bold, with **asterisks** or __underscores__.

Combined emphasis with **asterisks and _underscores_**.

Strikethrough uses two tildes. ~~Scratch this.~~

1. First ordered list item
2. Another item
⋅⋅* Unordered sub-list.
1. Actual numbers don't matter, just that it's a number
⋅⋅1. Ordered sub-list
4. And another item.

⋅⋅⋅You can have properly indented paragraphs within list items. Notice the blank line above, and the leading spaces (at least one, but we'll use three here to also align the raw Markdown).

⋅⋅⋅To have a line break without a paragraph, you will need to use two trailing spaces.⋅⋅
⋅⋅⋅Note that this line is separate, but within the same paragraph.⋅⋅
⋅⋅⋅(This is contrary to the typical GFM line break behaviour, where trailing spaces are not required.)

* Unordered list can use asterisks
- Or minuses
+ Or pluses

[I'm an inline-style link](https://www.google.com)

[I'm an inline-style link with title](https://www.google.com "Google's Homepage")

[I'm a reference-style link][Arbitrary case-insensitive reference text]

[I'm a relative reference to a repository file](../blob/master/LICENSE)

[You can use numbers for reference-style link definitions][1]

Or leave it empty and use the [link text itself].

URLs and URLs in angle brackets will automatically get turned into links.
http://www.example.com or <http://www.example.com> and sometimes
example.com (but not on Github, for example).

Some text to show that the reference links can follow later.

[arbitrary case-insensitive reference text]: https://www.mozilla.org
[1]: http://slashdot.org
[link text itself]: http://www.reddit.com

Here's our logo (hover to see the title text):

Inline-style:
![alt text](https://github.com/adam-p/markdown-here/raw/master/src/common/images/icon48.png "Logo Title Text 1")

Reference-style:
![alt text][logo]

[logo]: https://github.com/adam-p/markdown-here/raw/master/src/common/images/icon48.png "Logo Title Text 2"

Inline `code` has `back-ticks around` it.

```javascript
var s = "JavaScript syntax highlighting";
alert(s);
```

```python
s = "Python syntax highlighting"
print s
```

```
No language indicated, so no syntax highlighting.
But let's throw in a <b>tag</b>.
```

Here is a simple footnote[^1].

A footnote can also have multiple lines[^2].

You can also use words, to fit your writing style more closely[^note].

[^1]: My reference.
[^2]: Every new line should be prefixed with 2 spaces.
This allows you to have a footnote with multiple lines.
[^note]:
Named footnotes will still render with numbers instead of the text but allow easier identification and linking.
This footnote also has been made with a different syntax using 4 spaces for new lines.

Colons can be used to align columns.

| Tables        | Are           | Cool  |
| ------------- |:-------------:| -----:|
| col 3 is      | right-aligned | $1600 |
| col 2 is      | centered      |   $12 |
| zebra stripes | are neat      |    $1 |

There must be at least 3 dashes separating each header cell.
The outer pipes (|) are optional, and you don't need to make the
raw Markdown line up prettily. You can also use inline Markdown.

Markdown | Less | Pretty
--- | --- | ---
*Still* | `renders` | **nicely**
1 | 2 | 3

> Blockquotes are very handy in email to emulate reply text.
> This line is part of the same quote.

Quote break.

> This is a very long line that will still be quoted properly when it wraps. Oh boy let's keep writing to make sure this is long enough to actually wrap for everyone. Oh, you can *put* **Markdown** into a blockquote.


<dl>
<dt>Definition list</dt>
<dd>Is something people use sometimes.</dd>

  <dt>Markdown in HTML</dt>
  <dd>Does *not* work **very** well. Use HTML <em>tags</em>.</dd>
</dl>

Three or more...

---

Hyphens

***

Asterisks

___

Underscores

Here's a line for us to start with.

This line is separated from the one above by two newlines, so it will be a *separate paragraph*.

This line is also a separate paragraph, but...
This line is only separated by a single newline, so it's a separate line in the *same paragraph*.


[![IMAGE ALT TEXT HERE](http://img.youtube.com/vi/YOUTUBE_VIDEO_ID_HERE/0.jpg)](http://www.youtube.com/watch?v=YOUTUBE_VIDEO_ID_HERE)
[WARNING]
--
* Macro was not terminated in the file.
* @define HEADER($x)=$x
*  at /Users/verhasp/github/jamal/jamal-test/TEST_PLAYGROUND.md.jam/8:3
--
[source]
----
Macro was not terminated in the file.
@define HEADER($x)=$x
 at /Users/verhasp/github/jamal/jamal-test/TEST_PLAYGROUND.md.jam/8:3
javax0.jamal.api.BadSyntaxAt: Macro was not terminated in the file.
@define HEADER($x)=$x
 at /Users/verhasp/github/jamal/jamal-test/TEST_PLAYGROUND.md.jam/8:3
	at javax0.jamal.api.Macro.fetch(Macro.java:169)
	at javax0.jamal.engine.util.MacroBodyFetcher.getNextMacroBody(MacroBodyFetcher.java:49)
	at javax0.jamal.engine.Processor.getNextMacroBody(Processor.java:835)
	at javax0.jamal.engine.Processor.processMacro(Processor.java:241)
	at javax0.jamal.engine.Processor.process(Processor.java:169)
	at javax0.jamal.asciidoc.JamalPreprocessor.process(JamalPreprocessor.java:323)
	at javax0.jamal.asciidoc.JamalPreprocessor.runJamalInProcess(JamalPreprocessor.java:189)
	at javax0.jamal.asciidoc.JamalPreprocessor.process(JamalPreprocessor.java:121)
	at org.asciidoctor.jruby.extension.processorproxies.PreprocessorProxy.process(PreprocessorProxy.java:94)
	at org.asciidoctor.jruby.extension.processorproxies.PreprocessorProxy$INVOKER$i$2$0$process.call(PreprocessorProxy$INVOKER$i$2$0$process.gen)
	at org.jruby.internal.runtime.methods.JavaMethod$JavaMethodTwoOrN.call(JavaMethod.java:1035)
	at org.jruby.RubyMethod.call(RubyMethod.java:124)
	at org.jruby.RubyMethod$INVOKER$i$call.call(RubyMethod$INVOKER$i$call.gen)
	at org.jruby.internal.runtime.methods.JavaMethod$JavaMethodZeroOrOneOrTwoOrNBlock.call(JavaMethod.java:376)
	at org.jruby.runtime.callsite.CachingCallSite.call(CachingCallSite.java:204)
	at org.jruby.ir.interpreter.InterpreterEngine.processCall(InterpreterEngine.java:325)
	at org.jruby.ir.interpreter.StartupInterpreterEngine.interpret(StartupInterpreterEngine.java:72)
	at org.jruby.ir.interpreter.Interpreter.INTERPRET_BLOCK(Interpreter.java:116)
	at org.jruby.runtime.MixedModeIRBlockBody.commonYieldPath(MixedModeIRBlockBody.java:136)
	at org.jruby.runtime.IRBlockBody.doYield(IRBlockBody.java:170)
	at org.jruby.runtime.BlockBody.yield(BlockBody.java:108)
	at org.jruby.runtime.Block.yield(Block.java:188)
	at org.jruby.RubyArray.each(RubyArray.java:1865)
	at org.jruby.RubyArray$INVOKER$i$0$0$each.call(RubyArray$INVOKER$i$0$0$each.gen)
	at org.jruby.internal.runtime.methods.JavaMethod$JavaMethodZeroBlock.call(JavaMethod.java:560)
	at org.jruby.runtime.callsite.CachingCallSite.call(CachingCallSite.java:85)
	at org.jruby.runtime.callsite.CachingCallSite.callIter(CachingCallSite.java:94)
	at org.jruby.ir.instructions.CallBase.interpret(CallBase.java:546)
	at org.jruby.ir.interpreter.InterpreterEngine.processCall(InterpreterEngine.java:361)
	at org.jruby.ir.interpreter.StartupInterpreterEngine.interpret(StartupInterpreterEngine.java:72)
	at org.jruby.ir.interpreter.InterpreterEngine.interpret(InterpreterEngine.java:80)
	at org.jruby.internal.runtime.methods.MixedModeIRMethod.INTERPRET_METHOD(MixedModeIRMethod.java:164)
	at org.jruby.internal.runtime.methods.MixedModeIRMethod.call(MixedModeIRMethod.java:151)
	at org.jruby.internal.runtime.methods.DynamicMethod.call(DynamicMethod.java:210)
	at org.jruby.runtime.callsite.CachingCallSite.call(CachingCallSite.java:142)
	at org.jruby.ir.interpreter.InterpreterEngine.processCall(InterpreterEngine.java:345)
	at org.jruby.ir.interpreter.StartupInterpreterEngine.interpret(StartupInterpreterEngine.java:72)
	at org.jruby.ir.interpreter.InterpreterEngine.interpret(InterpreterEngine.java:92)
	at org.jruby.internal.runtime.methods.MixedModeIRMethod.INTERPRET_METHOD(MixedModeIRMethod.java:238)
	at org.jruby.internal.runtime.methods.MixedModeIRMethod.call(MixedModeIRMethod.java:225)
	at org.jruby.internal.runtime.methods.DynamicMethod.call(DynamicMethod.java:226)
	at org.jruby.runtime.callsite.CachingCallSite.call(CachingCallSite.java:204)
	at org.jruby.ir.interpreter.InterpreterEngine.processCall(InterpreterEngine.java:325)
	at org.jruby.ir.interpreter.StartupInterpreterEngine.interpret(StartupInterpreterEngine.java:72)
	at org.jruby.internal.runtime.methods.MixedModeIRMethod.INTERPRET_METHOD(MixedModeIRMethod.java:128)
	at org.jruby.internal.runtime.methods.MixedModeIRMethod.call(MixedModeIRMethod.java:115)
	at org.jruby.internal.runtime.methods.DynamicMethod.call(DynamicMethod.java:192)
	at org.jruby.RubyClass.finvoke(RubyClass.java:784)
	at org.jruby.runtime.Helpers.invoke(Helpers.java:661)
	at org.jruby.RubyBasicObject.callMethod(RubyBasicObject.java:370)
	at org.asciidoctor.jruby.internal.JRubyAsciidoctor.convert(JRubyAsciidoctor.java:311)
	at org.asciidoctor.jruby.internal.JRubyAsciidoctor.convert(JRubyAsciidoctor.java:336)
	at org.asciidoctor.jruby.internal.JRubyAsciidoctor.convert(JRubyAsciidoctor.java:331)
	at org.asciidoc.intellij.AsciiDocWrapper.render(AsciiDocWrapper.java:816)
	at org.asciidoc.intellij.AsciiDocWrapper.render(AsciiDocWrapper.java:782)
	at org.asciidoc.intellij.AsciiDocWrapper.render(AsciiDocWrapper.java:771)
	at org.asciidoc.intellij.editor.AsciiDocPreviewEditor.lambda$render$0(AsciiDocPreviewEditor.java:149)
	at org.asciidoc.intellij.editor.LazyApplicationPoolExecutor.lambda$execute$0(LazyApplicationPoolExecutor.java:66)
	at com.intellij.openapi.application.impl.ApplicationImpl$1.run(ApplicationImpl.java:246)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:539)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.util.concurrent.Executors$PrivilegedThreadFactory$1$1.run(Executors.java:702)
	at java.base/java.util.concurrent.Executors$PrivilegedThreadFactory$1$1.run(Executors.java:699)
	at java.base/java.security.AccessController.doPrivileged(AccessController.java:399)
	at java.base/java.util.concurrent.Executors$PrivilegedThreadFactory$1.run(Executors.java:699)
	at java.base/java.lang.Thread.run(Thread.java:833)
----
