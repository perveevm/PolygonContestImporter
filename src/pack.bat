javac -cp ..\lib\* -encoding utf-8 pcms2\*.java
jar cf polygon.jar pcms2\*.class
del pcms2\*.class