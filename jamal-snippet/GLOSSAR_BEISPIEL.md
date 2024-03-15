# Wie man ein Glossar in Markdown erstellt





In diesem Dokument beschreiben wir, wie Sie Ihrem Markdown-Dokument ein Glossar hinzufügen können.

Markdown ist eine einfache Auszeichnungssprache, die komplexe Funktionen wie ein Glossar nicht unterstützt.
Jedoch können Sie automatisch ein Glossar mit Hilfe der Jamal Meta-Markup erstellen.
Wenn Sie Jamal Meta-Markup zu Ihrem Markdown-Dokument hinzufügen, können Sie Begriffe und deren Definitionen definieren.
Sie können eine `.md.jam`-Datei bearbeiten und die verarbeitete `.md`-Datei mit dem Glossar am Ende des Dokuments erhalten.

Jamal ist eine freie Software und kann unter https://github.com/verhas/jamal gefunden werden.

## Was ist ein Glossar?

Ein Glossar ist ein Liste von Begriffen und deren Definitionen.
Es wird verwendet, um die Bedeutung von Begriffen in einem Dokument zu erklären.
Es wird üblicherweise am Ende des Dokuments platziert mit Definitionen in alphabetischer Reihenfolge.
Oft listen technische Dokumente auch Abkürzungen im Glossar auf.
Zum Beispiel, wenn ich den Begriff TLA verwende, der für Dreiletterabkürzung steht, würde ich ihn zum Glossar hinzufügen.
Er würde im Glossar wie folgt erscheinen:

TLA::  Dreiletterabkürzung

## Problem bei der Wartung von Glossaren

Es gibt einige Probleme bei der Wartung von Glossaren:

* Begriffe müssen am Ende des Dokuments für das Glossar bearbeitet werden.
Das ist nicht der Ort, wo das Glossar im Text verwendet wird.
Redakteure müssen zwischen dem Glossar und dessen Referenzen im gesamten Dokument wechseln, was den Zusammenhang verwandter Inhalte stören kann.

* Diese getrennte Lage von ansonsten verwandten Inhalten macht es schwierig, das Glossar zu pflegen.
Wenn neue Begriffe auftauchen, kann die Bearbeitung aufgeschoben und verpasst werden.
Ebenso, wenn Begriffe aus dem Text entfernt werden, wird das Glossar möglicherweise nicht aktualisiert.

* Das Glossar sollte alphabetisch sortiert sein.
Dies ist ein manueller Prozess und kann zeitaufwendig sein.

## Wie Jamal bei Glossaren zur Rettung kommt

Jamal ist eine Meta-Markup-Sprache, die es Ihnen ermöglicht, Makros zu definieren.
Das Markup wird vorverarbeitet, bevor die tatsächliche Formatierung des Dokuments verarbeitet wird.
Dieses Dokument ist ein Markdown-Dokument, und das Jamal-Markup wird verarbeitet, bevor das Markdown verarbeitet wird.
So können Sie `Ihr_Dokument.md.jam` haben, einschließlich all Ihres Markdowns und Jamal-Markups, und Sie erhalten die finale `Ihr_Dokument.md`-Datei mit allen verarbeiteten Makros.


Mit Jamal können Sie Makros definieren, die das Glossar während der Textverarbeitung erstellen, und das erstellte Glossar wird dann am Ende des Dokuments verwendet.


Die in dieser Datei definierten Makros sind folgende

```
{%@define GLOSSAR=%}{%@define begriff($BEGRIFF,$DEFINITION)={%#define GLOSSAR={%GLOSSAR%}$BEGRIFF:: $DEFINITION
%}{%@define $BEGRIFF=$DEFINITION%}%}

```

Zuerst definiert es das Makro `GLOSSAR` als einen leeren String.
Dann definiert es ein Makro `begriff`, das zwei Argumente nimmt, den Begriff und seine Definition.
Wenn dieses Makro verwendet wird, ersetzt es das `GLOSSAR` mit dem neuen Begriff, und seine Definition wird am Ende angehängt.
Jede Ze

ile wird einen Begriff und eine Definition enthalten, getrennt durch `::`.
Zusätzlich definiert die Verwendung des Makros `begriff` den gegebenen Begriff selbst als ein Makro, das durch seine Definition ersetzt wird.

Wenn Sie einen neuen Begriff irgendwo im Dokument definieren möchten, können Sie das Makro verwenden, wie im folgenden Beispiel:

```
{%begriff/TLA/Dreiletterabkürzung%}\

```

Das Makro definiert auch den Begriff selbst als ein Makro, somit können Sie `{%TLA%}` verwenden, um `Dreiletterabkürzung` zu erhalten, oder

```
{%begriff/Glossar/Liste von Begriffen und deren Definitionen%}\
Ein Glossar ist ein {%Glossar%}.

```

als ein zweites Beispiel.
Sie können die Schönheit der vollständigen Redundanz des Quellcodes des Dokuments sehen.

Am Ende des Dokuments können Sie ein Kapitel haben

```
## Glossar

{%#sort
{%GLOSSAR%}%}

```

Das Ergebnis kann am Ende dieses Dokuments gesehen werden.

> ANMERKUNGEN:

* Wenn Sie das Glossar sortieren, ist es wichtig, dass Ihre Definitionen Einzeiler sind.
* Um ein Beispiel für die Erstellung und Verwendung der Makros zu sehen, schauen Sie sich den Quellcode dieses Dokuments an, `GLOSSAR_BEISPIEL.md.jam`.


## Glossar


Glossar:: Liste von Begriffen und deren Definitionen
Jamal:: eine Meta-Markup-Sprache mit Makro-Fähigkeiten
Jamal:: meta-Markup-Sprache mit Makro-Fähigkeiten
TLA:: Dreiletterabkürzung