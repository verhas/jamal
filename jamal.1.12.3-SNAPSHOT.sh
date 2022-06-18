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

download "com/javax0/jamal" "jamal-cmd" "1.12.3-SNAPSHOT"


com.javax0.jamal:jamal-engine:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.engine[m
com.javax0:levenshtein:jar:1.0.0:compile[36m -- module levenshtein[m
com.javax0.jamal:jamal-api:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.api[m
com.javax0.jamal:jamal-tools:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.tools[m
com.javax0.jamal:jamal-core:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.core[m
com.javax0.jamal:jamal-snippet:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.snippet[m
com.javax0.jamal:jamal-scriptbasic:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.scriptbasic[m
com.scriptbasic:jscriptbasic:jar:2.1.1:compile[36m -- module jscriptbasic[0;1;33m (auto)[m
com.javax0.jamal:jamal-groovy:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.groovy[m
org.codehaus.groovy:groovy-jsr223:jar:3.0.11:compile[36m -- module org.codehaus.groovy.jsr223[0;1m [auto][m
org.codehaus.groovy:groovy:jar:3.0.11:compile[36m -- module org.codehaus.groovy[0;1m [auto][m
com.javax0.jamal:jamal-ruby:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.ruby[m
org.jruby:jruby-complete:jar:9.3.4.0:compile[36m -- module org.jruby.complete[0;1m [auto][m
com.javax0.jamal:jamal-plantuml:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.plantuml[m
net.sourceforge.plantuml:plantuml:jar:1.2022.5:compile[36m -- module plantuml[0;1;33m (auto)[m
com.javax0.jamal:jamal-jamal:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.jamal[m
com.javax0.jamal:jamal-yaml:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.yaml[m
org.yaml:snakeyaml:jar:1.30:compile[36m -- module org.yaml.snakeyaml[0;1m [auto][m
ognl:ognl:jar:3.3.2:compile[36m -- module ognl[0;1m [auto][m
org.javassist:javassist:jar:3.28.0-GA:compile[36m -- module javassist[0;1;33m (auto)[m
com.javax0.jamal:jamal-io:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.io[m
com.javax0.jamal:jamal-markdown:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.markdown[m
com.vladsch.flexmark:flexmark-all:jar:0.64.0:compile[36m -- module flexmark.all[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark:jar:0.64.0:compile[36m -- module flexmark[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-abbreviation:jar:0.64.0:compile[36m -- module flexmark.ext.abbreviation[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-util:jar:0.64.0:compile[36m -- module flexmark.util[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-admonition:jar:0.64.0:compile[36m -- module flexmark.ext.admonition[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-anchorlink:jar:0.64.0:compile[36m -- module flexmark.ext.anchorlink[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-aside:jar:0.64.0:compile[36m -- module flexmark.ext.aside[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-attributes:jar:0.64.0:compile[36m -- module flexmark.ext.attributes[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-autolink:jar:0.64.0:compile[36m -- module flexmark.ext.autolink[0;1;33m (auto)[m
org.nibor.autolink:autolink:jar:0.6.0:compile[36m -- module autolink[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-definition:jar:0.64.0:compile[36m -- module flexmark.ext.definition[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-emoji:jar:0.64.0:compile[36m -- module flexmark.ext.emoji[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-enumerated-reference:jar:0.64.0:compile[36m -- module flexmark.ext.enumerated.reference[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-escaped-character:jar:0.64.0:compile[36m -- module flexmark.ext.escaped.character[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-footnotes:jar:0.64.0:compile[36m -- module flexmark.ext.footnotes[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-gfm-issues:jar:0.64.0:compile[36m -- module flexmark.ext.gfm.issues[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:jar:0.64.0:compile[36m -- module flexmark.ext.gfm.strikethrough[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-gfm-tasklist:jar:0.64.0:compile[36m -- module flexmark.ext.gfm.tasklist[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-gfm-users:jar:0.64.0:compile[36m -- module flexmark.ext.gfm.users[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-gitlab:jar:0.64.0:compile[36m -- module flexmark.ext.gitlab[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-jekyll-front-matter:jar:0.64.0:compile[36m -- module flexmark.ext.jekyll.front.matter[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-jekyll-tag:jar:0.64.0:compile[36m -- module flexmark.ext.jekyll.tag[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-media-tags:jar:0.64.0:compile[36m -- module flexmark.ext.media.tags[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-resizable-image:jar:0.64.0:compile[36m -- module flexmark.ext.resizable.image[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-macros:jar:0.64.0:compile[36m -- module flexmark.ext.macros[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-ins:jar:0.64.0:compile[36m -- module flexmark.ext.ins[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-xwiki-macros:jar:0.64.0:compile[36m -- module flexmark.ext.xwiki.macros[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-superscript:jar:0.64.0:compile[36m -- module flexmark.ext.superscript[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-tables:jar:0.64.0:compile[36m -- module flexmark.ext.tables[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-toc:jar:0.64.0:compile[36m -- module flexmark.ext.toc[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-typographic:jar:0.64.0:compile[36m -- module flexmark.ext.typographic[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-wikilink:jar:0.64.0:compile[36m -- module flexmark.ext.wikilink[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-yaml-front-matter:jar:0.64.0:compile[36m -- module flexmark.ext.yaml.front.matter[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-ext-youtube-embedded:jar:0.64.0:compile[36m -- module flexmark.ext.youtube.embedded[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-html2md-converter:jar:0.64.0:compile[36m -- module flexmark.html2md.converter[0;1;33m (auto)[m
org.jsoup:jsoup:jar:1.14.3:compile[36m -- module org.jsoup[0;1m [auto][m
com.vladsch.flexmark:flexmark-jira-converter:jar:0.64.0:compile[36m -- module flexmark.jira.converter[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-pdf-converter:jar:0.64.0:compile[36m -- module flexmark.pdf.converter[0;1;33m (auto)[m
com.openhtmltopdf:openhtmltopdf-core:jar:1.0.10:compile[36m -- module openhtmltopdf.core[0;1;33m (auto)[m
com.openhtmltopdf:openhtmltopdf-pdfbox:jar:1.0.10:compile[36m -- module openhtmltopdf.pdfbox[0;1;33m (auto)[m
org.apache.pdfbox:pdfbox:jar:2.0.24:compile[36m -- module org.apache.pdfbox[0;1m [auto][m
org.apache.pdfbox:fontbox:jar:2.0.24:compile[36m -- module org.apache.fontbox[0;1m [auto][m
commons-logging:commons-logging:jar:1.2:compile[36m -- module commons.logging[0;1;33m (auto)[m
org.apache.pdfbox:xmpbox:jar:2.0.24:compile[36m -- module org.apache.xmpbox[0;1m [auto][m
de.rototor.pdfbox:graphics2d:jar:0.32:compile[36m -- module de.rototor.pdfbox.graphics2d[0;1m [auto][m
com.openhtmltopdf:openhtmltopdf-rtl-support:jar:1.0.10:compile[36m -- module openhtmltopdf.rtl.support[0;1;33m (auto)[m
com.ibm.icu:icu4j:jar:59.1:compile[36m -- module icu4j[0;1;33m (auto)[m
com.openhtmltopdf:openhtmltopdf-jsoup-dom-converter:jar:1.0.0:compile[36m -- module openhtmltopdf.jsoup.dom.converter[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-profile-pegdown:jar:0.64.0:compile[36m -- module flexmark.profile.pegdown[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-util-ast:jar:0.64.0:compile[36m -- module flexmark.util.ast[0;1;33m (auto)[m
org.jetbrains:annotations:jar:15.0:compile[36m -- module annotations[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-util-builder:jar:0.64.0:compile[36m -- module flexmark.util.builder[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-util-collection:jar:0.64.0:compile[36m -- module flexmark.util.collection[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-util-data:jar:0.64.0:compile[36m -- module flexmark.util.data[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-util-dependency:jar:0.64.0:compile[36m -- module flexmark.util.dependency[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-util-format:jar:0.64.0:compile[36m -- module flexmark.util.format[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-util-html:jar:0.64.0:compile[36m -- module flexmark.util.html[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-util-misc:jar:0.64.0:compile[36m -- module flexmark.util.misc[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-util-options:jar:0.64.0:compile[36m -- module flexmark.util.options[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-util-sequence:jar:0.64.0:compile[36m -- module flexmark.util.sequence[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-util-visitor:jar:0.64.0:compile[36m -- module flexmark.util.visitor[0;1;33m (auto)[m
com.vladsch.flexmark:flexmark-youtrack-converter:jar:0.64.0:compile[36m -- module flexmark.youtrack.converter[0;1;33m (auto)[m
com.javax0.jamal:jamal-assertions:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.assertions[m
com.javax0.jamal:jamal-word:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.word[m
org.apache.poi:poi-ooxml:jar:5.2.2:compile[36m -- module org.apache.poi.ooxml[m
org.apache.poi:poi:jar:5.2.2:compile[36m -- module org.apache.poi.poi[m
commons-codec:commons-codec:jar:1.15:compile[36m -- module org.apache.commons.codec[0;1m [auto][m
org.apache.commons:commons-math3:jar:3.6.1:compile[36m -- module commons.math3[0;1;33m (auto)[m
com.zaxxer:SparseBitSet:jar:1.2:compile[36m -- module SparseBitSet[0;1;33m (auto)[m
org.apache.poi:poi-ooxml-lite:jar:5.2.2:compile[36m -- module org.apache.poi.ooxml.schemas[m
org.apache.xmlbeans:xmlbeans:jar:5.0.3:compile[36m -- module org.apache.xmlbeans[m
org.apache.commons:commons-compress:jar:1.21:compile[36m -- module org.apache.commons.compress[0;1m [auto][m
commons-io:commons-io:jar:2.11.0:compile[36m -- module org.apache.commons.io[0;1m [auto][m
com.github.virtuald:curvesapi:jar:1.07:compile[36m -- module com.github.virtuald.curvesapi[0;1m [auto][m
org.apache.logging.log4j:log4j-api:jar:2.17.2:compile[36m -- module org.apache.logging.log4j[m
org.apache.commons:commons-collections4:jar:4.4:compile[36m -- module org.apache.commons.collections4[0;1m [auto][m
com.javax0.jamal:jamal-mock:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.mock[m
com.javax0.jamal:jamal-testsupport:jar:1.12.3-SNAPSHOT:compile[36m -- module jamal.testsupport[m


java -cp "$CLASSPATH" javax0.jamal.cmd.JamalMain $*