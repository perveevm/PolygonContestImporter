# PolygonContestImporter
Import contest settings from polygon.codeforces.com to PCMS2 format.

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
 

#### import.properties example

```
vfs = R:\pcms\vfs
webroot = C:\Programs\tomcat\www\root\statements
defaultLanguage = russian
```
