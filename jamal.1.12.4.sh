#!/usr/bin/env bash



CLASSPATH=""

function download() {
  DIR=${1//\.//}
  if ! test -f $HOME/.m2/repository/$DIR/$2/$3/$2-$3.jar; then
    echo "downloading https://repo1.maven.org/maven2/$DIR/$2/$3/jamal-$2-$3.jar"
    mkdir -p $HOME/.m2/repository/$DIR/$2/$3
    if command -v wget &>/dev/null; then
      wget --no-check-certificate https://repo1.maven.org/maven2/$DIR/$2/$3/$2-$3.jar -O $HOME/.m2/repository/$DIR/$2/$3/$2-$3.jar
    else
      if command -v curl &>/dev/null; then
        curl https://repo1.maven.org/maven2/$DIR/$2/$3/jamal-$2-$3.jar -o $HOME/.m2/repository/$DIR/$2/$3/$2-$3.jar
      else
        echo "There is no curl nor wget available"
        exit 255
      fi
    fi
  fi
CLASSPATH=$CLASSPATH:$HOME/.m2/repository/$DIR/$2/$3/$2-$3.jar
}

download "com/javax0/jamal" "jamal-cmd" "1.12.4"


download "com.javax0.jamal" "jamal-engine" "1.12.4"
download "com.javax0" "levenshtein" "1.0.0"
download "com.javax0.jamal" "jamal-api" "1.12.4"
download "com.javax0.jamal" "jamal-tools" "1.12.4"
download "com.javax0.jamal" "jamal-core" "1.12.4"
download "com.javax0.jamal" "jamal-snippet" "1.12.4"
download "com.javax0.jamal" "jamal-scriptbasic" "1.12.4"
download "com.scriptbasic" "jscriptbasic" "2.1.1"
download "com.javax0.jamal" "jamal-groovy" "1.12.4"
download "org.codehaus.groovy" "groovy-jsr223" "3.0.12"
download "org.codehaus.groovy" "groovy" "3.0.12"
download "com.javax0.jamal" "jamal-ruby" "1.12.4"
download "org.jruby" "jruby-complete" "9.3.8.0"
download "com.javax0.jamal" "jamal-plantuml" "1.12.4"
download "net.sourceforge.plantuml" "plantuml" "1.2022.7"
download "com.javax0.jamal" "jamal-jamal" "1.12.4"
download "com.javax0.jamal" "jamal-yaml" "1.12.4"
download "org.yaml" "snakeyaml" "1.32"
download "ognl" "ognl" "3.3.2"
download "org.javassist" "javassist" "3.28.0-GA"
download "com.javax0.jamal" "jamal-io" "1.12.4"
download "com.javax0.jamal" "jamal-markdown" "1.12.4"
download "com.vladsch.flexmark" "flexmark-all" "0.64.0"
download "com.vladsch.flexmark" "flexmark" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-abbreviation" "0.64.0"
download "com.vladsch.flexmark" "flexmark-util" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-admonition" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-anchorlink" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-aside" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-attributes" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-autolink" "0.64.0"
download "org.nibor.autolink" "autolink" "0.6.0"
download "com.vladsch.flexmark" "flexmark-ext-definition" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-emoji" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-enumerated-reference" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-escaped-character" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-footnotes" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-gfm-issues" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-gfm-strikethrough" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-gfm-tasklist" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-gfm-users" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-gitlab" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-jekyll-front-matter" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-jekyll-tag" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-media-tags" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-resizable-image" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-macros" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-ins" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-xwiki-macros" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-superscript" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-tables" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-toc" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-typographic" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-wikilink" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-yaml-front-matter" "0.64.0"
download "com.vladsch.flexmark" "flexmark-ext-youtube-embedded" "0.64.0"
download "com.vladsch.flexmark" "flexmark-html2md-converter" "0.64.0"
download "org.jsoup" "jsoup" "1.14.3"
download "com.vladsch.flexmark" "flexmark-jira-converter" "0.64.0"
download "com.vladsch.flexmark" "flexmark-pdf-converter" "0.64.0"
download "com.openhtmltopdf" "openhtmltopdf-core" "1.0.10"
download "com.openhtmltopdf" "openhtmltopdf-pdfbox" "1.0.10"
download "org.apache.pdfbox" "pdfbox" "2.0.24"
download "org.apache.pdfbox" "fontbox" "2.0.24"
download "commons-logging" "commons-logging" "1.2"
download "org.apache.pdfbox" "xmpbox" "2.0.24"
download "de.rototor.pdfbox" "graphics2d" "0.32"
download "com.openhtmltopdf" "openhtmltopdf-rtl-support" "1.0.10"
download "com.ibm.icu" "icu4j" "59.1"
download "com.openhtmltopdf" "openhtmltopdf-jsoup-dom-converter" "1.0.0"
download "com.vladsch.flexmark" "flexmark-profile-pegdown" "0.64.0"
download "com.vladsch.flexmark" "flexmark-util-ast" "0.64.0"
download "org.jetbrains" "annotations" "15.0"
download "com.vladsch.flexmark" "flexmark-util-builder" "0.64.0"
download "com.vladsch.flexmark" "flexmark-util-collection" "0.64.0"
download "com.vladsch.flexmark" "flexmark-util-data" "0.64.0"
download "com.vladsch.flexmark" "flexmark-util-dependency" "0.64.0"
download "com.vladsch.flexmark" "flexmark-util-format" "0.64.0"
download "com.vladsch.flexmark" "flexmark-util-html" "0.64.0"
download "com.vladsch.flexmark" "flexmark-util-misc" "0.64.0"
download "com.vladsch.flexmark" "flexmark-util-options" "0.64.0"
download "com.vladsch.flexmark" "flexmark-util-sequence" "0.64.0"
download "com.vladsch.flexmark" "flexmark-util-visitor" "0.64.0"
download "com.vladsch.flexmark" "flexmark-youtrack-converter" "0.64.0"
download "com.javax0.jamal" "jamal-assertions" "1.12.4"
download "com.javax0.jamal" "jamal-word" "1.12.4"
download "org.apache.poi" "poi-ooxml" "5.2.2"
download "org.apache.poi" "poi" "5.2.2"
download "commons-codec" "commons-codec" "1.15"
download "org.apache.commons" "commons-math3" "3.6.1"
download "com.zaxxer" "SparseBitSet" "1.2"
download "org.apache.poi" "poi-ooxml-lite" "5.2.2"
download "org.apache.xmlbeans" "xmlbeans" "5.0.3"
download "org.apache.commons" "commons-compress" "1.21"
download "commons-io" "commons-io" "2.11.0"
download "com.github.virtuald" "curvesapi" "1.07"
download "org.apache.logging.log4j" "log4j-api" "2.17.2"
download "org.apache.commons" "commons-collections4" "4.4"
download "com.javax0.jamal" "jamal-mock" "1.12.4"
download "com.javax0.jamal" "jamal-testsupport" "1.12.4"


java -cp "$CLASSPATH" javax0.jamal.cmd.JamalMain $*