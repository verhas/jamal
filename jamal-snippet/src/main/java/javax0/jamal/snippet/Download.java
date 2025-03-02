package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Scanner;

import java.io.FileOutputStream;
import java.io.IOException;

@Macro.Name("download")
public
class Download implements Macro, Scanner {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var to = scanner.file(null, "file");
        scanner.done();

        final var reference = in.getReference();
        final var fromName = FileTools.absolute(reference, in.toString().trim());

        try (final var fos = new FileOutputStream(to.file())) {
            final var bytes = FileTools.getFileBinaryContent(fromName, true, processor);
            fos.write(bytes);
        } catch (IOException ioException) {
            throw new BadSyntax(String.format("There was an IOException downloading the file '%s' to '%s'", fromName, to.get()), ioException);
        }
        return "";
    }

}
/*template jm_download
{template |download|download (file=$F$) $URL$|download file from the net|
  {variable |F|fileRelativePath()}
  {variable |URL|"https://"}
}
 */
/* snippet Download

This macro downloads a resource from a URL and saves it to a file.

The format of the macro is
{%sample/
  {@download (file="file name") URL}
%}

There is one parameter:

* `file` should specify the file where to save the content of the downloaded file.

The URL is given after the option.

[NOTE]
====
. There is a similar functionality macro in the `jamal-io` module.
That macro is called `io:copy`.
This macro cannot append to a file, will not create the directory if it does not exist, you cannot control the use of cache.
This macro will not use the Jamal download cache.

. Technically, the URL can specify any file name that you could `include`.
You can specify the usual `maven:`, `res:`, etc. prefixes in addition to `https:`.

. This macro is intended to be used together with the macro `memoize`.
For this reason it does not use the cache mechanism of Jamal.
====

end snippet*/
