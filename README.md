# warc2sitemap

<!---
[![start with why](https://img.shields.io/badge/start%20with-why%3F-brightgreen.svg?style=flat)](http://www.ted.com/talks/simon_sinek_how_great_leaders_inspire_action)
--->
[![GitHub release](https://img.shields.io/github/release/elbosso/warc2sitemap/all.svg?maxAge=1)](https://GitHub.com/elbosso/warc2sitemap/releases/)
[![GitHub tag](https://img.shields.io/github/tag/elbosso/warc2sitemap.svg)](https://GitHub.com/elbosso/warc2sitemap/tags/)
[![GitHub license](https://img.shields.io/github/license/elbosso/warc2sitemap.svg)](https://github.com/elbosso/warc2sitemap/blob/master/LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/elbosso/warc2sitemap.svg)](https://GitHub.com/elbosso/warc2sitemap/issues/)
[![GitHub issues-closed](https://img.shields.io/github/issues-closed/elbosso/warc2sitemap.svg)](https://GitHub.com/elbosso/warc2sitemap/issues?q=is%3Aissue+is%3Aclosed)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/elbosso/warc2sitemap/issues)
[![GitHub contributors](https://img.shields.io/github/contributors/elbosso/warc2sitemap.svg)](https://GitHub.com/elbosso/warc2sitemap/graphs/contributors/)
[![Github All Releases](https://img.shields.io/github/downloads/elbosso/warc2sitemap/total.svg)](https://github.com/elbosso/warc2sitemap)
[![Website elbosso.github.io](https://img.shields.io/website-up-down-green-red/https/elbosso.github.io.svg)](https://elbosso.github.io/)

## Overview

This project is intended to turn a [WARC file](https://en.wikipedia.org/wiki/Web_ARChive) into a sitemap or into something (a graph description)
one could build a sitemap from. The first release only offers to create a [Graphviz](https://graphviz.org/) file that can 
then be rendered - for example into [SVG](https://en.wikipedia.org/wiki/Scalable_Vector_Graphics).

## Inner workings

WARC files can be created for example by using [wget](https://wiki.archiveteam.org/index.php/Wget_with_WARC_output). The application 
currently only supports uncompressed WARC files.

* The WARC file is scanned for *response* objects
* Of those found, only the ones having media type *text/html* are considered
* The path of the URLs of those remaining is then checked against the regular expressions on the whitelist (if such are given)
* If the path matches at least one of the expressions or none are given, it is matched against the egular expressions on the blacklist (if such are given)
* If the path matches none of those expressions - ore none are given, the body of the response is then loaded and filtered:
* For each xpath from the list of xpaths to search, the xpath is expanded by _//a/@href_ and then applied to the body (if no xpaths are given, only _//a/@href_ is applied)
* From the Result of the combined xpath queries, doubles are removed and after that, the remaining results are the pages, the currently analyzed one is connected to

The whitelist, blacklist and list of xpaths to search can be configured by writing textfiles where each line contains exactly one item. Examples
for that can be found inside directory _examples_.

## Build
```shell
mvn -U package assembly:single
```

## Run
```shell
$JAVA_HOME/bin/java -jar target/warc2sitemap-<version>-jar-with-dependencies.jar \
-w examples/whitelist.txt -b examples/blacklist.txt -x examples/xpathsToSearch.txt \
-i input.warc -o output.gv
```

## Render

After rendering it using for example
```shell
dot -Tsvg /tmp/output.gv >/tmp/sitemap.svg
```

the resulting output looks like this (cutout from a much larger sitemap of [elbosso.github.io](https://elbosso.github.io))

![Example rendering](examples/output.png)

As should be obvious: the line thickness of the bubble, the fontsize of the label inside the bubble and the margin around the label inside the bubble grow with the numbers of connections for each bubble.
