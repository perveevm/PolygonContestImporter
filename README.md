# PolygonContestImporter

[![Build](https://github.com/ilsaf13/PolygonContestImporter/actions/workflows/CI.yml/badge.svg)](https://github.com/ilsaf13/PolygonContestImporter/actions)
[![codecov](https://codecov.io/gh/ilsaf13/PolygonContestImporter/branch/master/graph/badge.svg)](https://codecov.io/gh/ilsaf13/PolygonContestImporter)
[![Release](https://shields.io/github/v/release/ilsaf13/PolygonContestImporter?display_name=tag)](https://github.com/ilsaf13/PolygonContestImporter/releases/latest)
[![Commit Activity](https://shields.io/github/commit-activity/m/ilsaf13/PolygonContestImporter)](https://github.com/ilsaf13/PolygonContestImporter/commits/master)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)


Import contest settings from polygon.codeforces.com to PCMS2 format.

## Current version

Download from [here](https://github.com/ilsaf13/PolygonContestImporter/releases).

Or clone this repository and run `mvn package`.

## First try
You can start by downloading contest package, say contest-XXXX.zip, extract to contest-XXXX directory.
Run doall to generate tests. Then you can run PolygonContestImporter:

`java -jar importer-jar-with-dependencies.jar contest auto icpc contest-XXXX`

You will get `challenge.xml` file generated in `contest-XXXX` directory, `problem.xml` in each problem will be renamed 
to `problem.xml.polygon`, and new `problem.xml` is created, which is PCMS2 `problem.xml`.

We tried `auto` instead of challenge id, these means `challenge-id = com.codeforces.polygon.<contestid>`, and 
`problem-id = com.codeforces.polygon.<problem owner>.<problem short name>`.

You can use something else instead auto, i.e. `demo.icpc`, then `challenge-id = demo.icpc` and 
`problem-id = demo.icpc.<problem short name>`

## Properties

Main properties are located in `import.properties` file.
This file should be located in the same directory as PolygonContestImporter `jar` file.
Properties that can be used in `import.properties`:
 - `vfs` — path to PCMS2 vfs, if defined and non-null, then importer will copy problem files to PCMS2 vfs.
If the files are already in vfs, importer will ask to replace
 - `webroot` — path to statements root directory, if defined and non-null, then importer will copy problem statements there
 - `defaultLanguage` — the problem names are extracted in this language by default
 - `polygonUsername` — the username in polygon that is used to download packages and descriptors
 - `polygonPassword` — the password for polygon username

#### import.properties example

```
vfs = R:/pcms/vfs
webroot = C:/Programs/tomcat/www/root/statements
defaultLanguage = russian
```

## What it actually does?

### Problem processing

For problem importing our importer parses `problem.xml`, and creates both `%ioi` and `%icpc` scripts inside PCMS2 problem.xml.

First, importer looks for `problem.xml.polygon` file inside problem directory. If there is no such file, it looks for `problem.xml` file, and renames it to `problem.xml.polygon`. Then `problem.xml.polygon` file is getting parsed, creating `problem.xml` file, which is PCMS2 problem descriptor. So if anything fails during the import, importer can be run again.

#### IOI importing, groups, feedback

Importer only takes `tests` testset from polygon, and creates `main` testset in PCMS2, for `%ioi` testing script. Other testsets are ignored. The groupnames should be integers, the greater test number should have the group number greater or equal. All tests should have group numbers. Group `0` considered sample tests, so `statistics` feedback, and `sum` scoring, for this group is generated. For other groups feedback and scoring is generated according to similar ones in polygon.

#### Checker importing

The checker binary is taken, it's not being compiled when importing.

### Contest processing

For contest importing the importer parses `contest.xml`, and imports all problems one by one. It creates `challenge.xml` with problem names in language of `defaultLanguage` parameter in `import.properties`. Also `submit.lst` is created with `session-id` equal to `<challenge-id>.0`. 

### Copying to VFS

If `vfs` parameter is defined and non-null, then the imported problem(s), are copied to `vfs/problems/<problem-id-prefix>/<short-name>`, and `challenge.xml` and `submit.lst` to `vfs/etc/<challenge-id>`. The importer asks to replace, if the file or directory already exists. `--y` parameter says `y` to all those questions automatically.

### Copying to Web

If `webroot` parameter is defined and non-null, then if `<problem-dir>/statements/<defaultLanguage>/statements.pdf` exists, it is getting copied to `<webroot>/statements/<challenge-id>/statements.pdf`.

## Downloading packages

In version 1.2 downloading packages feature introduced. There are two commands `download-contest` and `download-problem`. Each of them downloads the last revision package, and behaves as `contest` and `problem` command would behave after. 

### Authorization

You have to provide polygon username and password for authentication. `polygonUsername` and `polygonPassword` can be defined in `import.properties`. There is also a way to override those properties with command line parameters: `-u` (`--user`) and `-p` (`--password`), each with an argument to follow. Password argument can be ommitted, so the importer will ask you the password to type to stdin (the letters you type won't be shown). The example of the command to import the contest with id 3412 is:

```sh
java -jar importer-jar-with-dependencies.jar download-contest auto ioi 3412 -u ACRush -p
```

### Tests generation

If the problem doesn't have full package generated for the last revision that has a standard package, then standard package is downloaded. For standard packages importer runs `doall.bat` for windows (`doall.sh` for other OS), if the script fails, then the importing is terminated.

### Generated contest statement downloading

When downloading the contest, all problem statement pdf-files specified in `contest.xml` are downloaded. So, even if you don't have LaTeX installed, the problem statements can be uploaded.

### Contest downloading

Currently for contest downloading only `https://polygon.codeforces.com` polygon instance is supported.

### Temporary files

All downloaded data are temporarily saved to files inside TMP folder (the path depends on OS you are using, for instance default one for some Linux distributives is `/tmp`). At the end of importing, the importer will ask to remove those files.
