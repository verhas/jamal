#!/usr/bin/env bash

cat <<END
-------------------------------------------------------
 M A N U A L   R E L E A S E  I S   N O T   A V A I L
-------------------------------------------------------
Execute this file to get this message.

Normal release should

1. Edit the version.jim file and update the release version.
2. cd jamal-debug-ui
3. $ ./deployprod

This will ensure that that the version number of the React
client is the same as the version of the application. It is
not really the version number, which is important, but that
is used to ensure compatibility between the debugger server
and client.

4. cd ..
5. mvn -f genpom.xml clean
6. mvn verify

At this point we can be sure that there are no errors. If
there are errors then they have to be fixed.

If all compilation went well, then and only then:

7.  git commit -m "new release <VERSION>"

push all changes to github

8.  mvn deploy -Prelease

publish the release

9. When the release went well, then create a new release tag in GutHub.
-------------------------------------------------------

END